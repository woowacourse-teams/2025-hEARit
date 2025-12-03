import 'dart:async';

import 'package:flutter/material.dart';

import '../../core/audio/hearit_player_controller.dart';
import '../detail/hearit_detail_viewmodel.dart';
import 'explore_models.dart';
import 'explore_repository.dart';

class ExploreViewModel extends ChangeNotifier {
  ExploreViewModel({ExploreRepository? repository, HearitPlayerController? controller})
    : _repository = repository ?? ExploreRepository(),
      playerController = controller! {
    _playerListener = _playerListenerImpl;
    playerController.addListener(_playerListener);
  }

  final ExploreRepository _repository;
  final HearitPlayerController playerController;
  late final VoidCallback _playerListener;

  final String pitchLine = '1분 미리듣기로 원하는 히어릿을 골라 들으세요!';

  final List<ExploreFeedItem> _items = [];
  int _nextCursorId = 0;
  bool _isLoadingPage = false;
  bool _hasMore = true;
  bool _initialLoading = true;
  String? _initialError;
  int _activeIndex = 0;
  bool _playbackEnabled = false;
  bool _completionHandled = false;
  bool _userSeeking = false;
  String? _currentSourceUrl;
  VoidCallback? _onActiveCompleted;
  Duration? _resumePosition;

  Duration _position = Duration.zero;
  Duration _totalDuration = const Duration(seconds: 12);

  List<ExploreFeedItem> get items => List.unmodifiable(_items);
  ExploreFeedItem? get activeItem =>
      _items.isNotEmpty ? _items[_activeIndex] : null;
  int get activeIndex => _activeIndex;
  bool get initialLoading => _initialLoading;
  String? get initialError => _initialError;
  bool get isLoadingMore => _isLoadingPage && _items.isNotEmpty;
  Duration get position => _position;
  bool get isPlaying => playerController.isPlaying;
  ValueNotifier<double> get progressNotifier => _progressNotifier;
  bool get playbackEnabled => _playbackEnabled;

  final ValueNotifier<double> _progressNotifier = ValueNotifier<double>(0);

  Future<void> loadInitial() async {
    _items.clear();
    _nextCursorId = 0;
    _hasMore = true;
    _activeIndex = 0;
    _position = Duration.zero;
    _progressNotifier.value = 0;
    _initialLoading = true;
    _initialError = null;
    notifyListeners();
    try {
      await _fetchPage();
      if (_items.isNotEmpty) {
        await ensureDetailsLoaded(0);
      }
    } catch (error, stack) {
      debugPrint('ExploreViewModel.loadInitial error: $error\n$stack');
      _initialError = error.toString();
    } finally {
      _initialLoading = false;
      notifyListeners();
    }
  }

  Future<void> loadMore() async {
    if (_isLoadingPage || !_hasMore) return;
    try {
      await _fetchPage();
    } catch (error, stack) {
      debugPrint('ExploreViewModel.loadMore error: $error\n$stack');
    }
  }

  Future<void> _fetchPage() async {
    _isLoadingPage = true;
    notifyListeners();
    try {
      final page = await _repository.fetchExplorePage(cursorId: _nextCursorId);
      if (page.items.isEmpty) {
        _hasMore = false;
      } else {
        _items.addAll(page.items);
        _nextCursorId = page.items.last.cursorId;
      }
    } finally {
      _isLoadingPage = false;
      notifyListeners();
    }
  }

  void setActiveIndex(int index) {
    if (index < 0 || index >= _items.length) return;
    _activeIndex = index;
    _position = Duration.zero;
    _completionHandled = true; // lock until new source is loaded
    _progressNotifier.value = 0;
    notifyListeners();
    ensureDetailsLoaded(index).then((_) {
      if (_playbackEnabled) {
        _maybePlayItem(_items[index]);
      }
    });
  }

  Future<void> ensureDetailsLoaded(int index) async {
    if (index < 0 || index >= _items.length) return;
    final item = _items[index];
    if (item.detailLoaded || item.detailLoading) return;

    item.detailLoading = true;
    notifyListeners();
    try {
      final assets = await _repository.fetchShortAssets(item.id);
      item.shortAudioUrl = assets.shortAudioUrl;
      item.scriptFileUrl = assets.scriptUrl;
      if (item.scriptFileUrl != null && item.scriptFileUrl!.isNotEmpty) {
        item.scripts = await _repository.fetchScriptsFromUrl(item.scriptFileUrl!);
        if (item.scripts != null && item.scripts!.isNotEmpty) {
          _totalDuration = item.scripts!.last.end;
          playerController.setExternalDuration(_totalDuration);
        }
      }
      item.detailLoaded = true;
      _completionHandled = true; // will unlock on new source load
    } finally {
      item.detailLoading = false;
      notifyListeners();
    }
  }

  Future<void> _maybePlayItem(ExploreFeedItem item,
      {Duration? startPosition}) async {
    if (item.shortAudioUrl == null || item.shortAudioUrl!.isEmpty) return;
    _currentSourceUrl = item.shortAudioUrl;
    await playerController.loadSource(item.shortAudioUrl!);
    final start = startPosition ?? Duration.zero;
    await playerController.seek(start);
    _position = start;
    final durationMs = _totalDuration.inMilliseconds;
    _progressNotifier.value =
        durationMs > 0 && start > Duration.zero
            ? (start.inMilliseconds / durationMs).clamp(0.0, 1.0)
            : 0;
    _completionHandled = false; // allow completion for this source
    if (!playerController.isPlaying) {
      await playerController.togglePlayback();
    }
  }

  Future<void> pauseAudio() async {
    await playerController.pause();
  }

  Future<void> setPlaybackEnabled(bool enabled) async {
    _playbackEnabled = enabled;
    if (!enabled) {
      _resumePosition = _position;
      await pauseAudio();
      return;
    }
    final current = activeItem;
    if (current != null && current.detailLoaded) {
      await _maybePlayItem(current, startPosition: _resumePosition);
      _resumePosition = null;
    }
  }

  void setOnCompleted(VoidCallback? callback) {
    _onActiveCompleted = callback;
  }

  void beginUserSeek() {
    _userSeeking = true;
    _completionHandled = true; // block auto-advance while dragging
  }

  void updateTempProgress(double fraction) {
    final clamped = fraction.clamp(0.0, 1.0);
    if (_progressNotifier.value != clamped) {
      _progressNotifier.value = clamped;
    }
  }

  Future<void> endUserSeek(double fraction) async {
    _userSeeking = false;
    await seekToFraction(fraction);
  }

  Future<void> seekToFraction(double fraction) async {
    if (_totalDuration == Duration.zero) return;
    final clamped = fraction.clamp(0.0, 1.0);
    final target = Duration(
      milliseconds: (_totalDuration.inMilliseconds * clamped).round(),
    );
    _completionHandled = false;
    _position = target;
    await playerController.seek(target);
    notifyListeners();
  }

  void _playerListenerImpl() {
    _position = playerController.position;
    if (playerController.duration != Duration.zero) {
      _totalDuration = playerController.duration;
    }
    final durationMs = _totalDuration.inMilliseconds;
    if (!_userSeeking && durationMs > 0) {
      final value = (_position.inMilliseconds / durationMs).clamp(0.0, 1.0);
      if (_progressNotifier.value != value) {
        _progressNotifier.value = value;
      }
    }
    if (_playbackEnabled &&
        activeItem != null &&
        activeItem!.detailLoaded &&
        playerController.isCompleted &&
        !_completionHandled &&
        !_userSeeking) {
      _completionHandled = true;
      _onActiveCompleted?.call();
    }
    notifyListeners();
  }

  /// Returns true if now playing after toggle, false if paused.
  Future<bool> togglePlayPause() async {
    final current = activeItem;
    if (current == null) return playerController.isPlaying;
    if (!current.detailLoaded) {
      await ensureDetailsLoaded(_activeIndex);
    }
    if (playerController.isPlaying) {
      await pauseAudio();
      return false;
    }

    // Not playing: ensure playback is allowed and source is ready.
    if (!_playbackEnabled) {
      await setPlaybackEnabled(true);
    }
    if (current.shortAudioUrl == null || current.shortAudioUrl!.isEmpty) {
      return false;
    }
    final sameSource = _currentSourceUrl == current.shortAudioUrl;
    if (sameSource && !playerController.isCompleted) {
      _completionHandled = false;
      await playerController.togglePlayback();
      return true;
    }
    await _maybePlayItem(current);
    return true;
  }

  @override
  void dispose() {
    playerController.removeListener(_playerListener);
    playerController.dispose();
    _progressNotifier.dispose();
    super.dispose();
  }
}
