import 'dart:async';

import 'package:audio_service/audio_service.dart';
import 'package:flutter/material.dart';
import 'package:just_audio/just_audio.dart';

import 'audio_handler.dart';
import 'playing_history_service.dart';

class HearitPlayerController extends ChangeNotifier {
  HearitPlayerController({
    required LocalAudioHandler audioHandler,
    PlayingHistoryService? playingHistoryService,
    double initialSpeed = 1.0,
  }) : _audioHandler = audioHandler,
       _playingHistoryService =
           playingHistoryService ?? PlayingHistoryService() {
    _currentSpeed = initialSpeed;

    _audioHandler.setSpeed(initialSpeed);

    // Listen: duration updates
    _durationSub = _audioHandler.durationStream.listen((duration) {
      _duration = duration ?? Duration.zero;

      // Update mediaItem with new duration
      if (_currentMediaItem != null) {
        final updated = _currentMediaItem!.copyWith(duration: _duration);
        _currentMediaItem = updated;
        _audioHandler.mediaItem.add(updated);
      }

      notifyListeners();
    });

    // Listen: position updates
    _positionSub = _audioHandler.positionStream.listen((position) {
      _position = position;
      notifyListeners();
    });

    // Listen: play/pause/processing state
    _playerStateSub = _audioHandler.playerStateStream.listen((state) {
      _handlePlayerStateChange(state);
    });
  }

  late final LocalAudioHandler _audioHandler;
  final PlayingHistoryService _playingHistoryService;

  Duration _duration = Duration.zero;
  Duration _position = Duration.zero;
  PlayerState? _latestState;
  double _currentSpeed = 1.0;

  MediaItem? _currentMediaItem;

  Duration get duration => _duration;
  Duration get position => _position;
  double get currentSpeed => _currentSpeed;
  MediaItem? get currentMediaItem => _currentMediaItem;

  bool get isPlaying => _latestState?.playing ?? false;

  bool get isBuffering {
    final s = _latestState?.processingState;
    return s == ProcessingState.loading || s == ProcessingState.buffering;
  }

  bool get isCompleted =>
      _latestState?.processingState == ProcessingState.completed;

  late final StreamSubscription<Duration?> _durationSub;
  late final StreamSubscription<Duration> _positionSub;
  late final StreamSubscription<PlayerState> _playerStateSub;

  // ──────────────────────────────
  // PlayerState 변화 처리 (재생 기록 저장 포함)
  // ──────────────────────────────
  void _handlePlayerStateChange(PlayerState state) {
    final prevState = _latestState;
    _latestState = state;

    // 1. 재생 완료 감지
    if (state.processingState == ProcessingState.completed) {
      _saveCurrentProgress('재생 완료');
    }

    // 2. 일시정지 감지 (playing: true → false)
    // PlayerState 리스너에서 감지되는 자동 일시정지 (전화, 블루투스 해제 등)
    if (prevState?.playing == true && state.playing == false) {
      _saveCurrentProgress('자동 일시정지');
    }

    notifyListeners();
  }

  // ──────────────────────────────
  // Controls
  // ──────────────────────────────
  Future<void> play() async => _audioHandler.play();

  Future<void> togglePlayback() async {
    if (isPlaying) {
      await pause();
    } else {
      await _audioHandler.play();
    }
  }

  Future<void> pause() async {
    await _audioHandler.pause();
    // 일시정지 시 재생 기록 저장
    await _saveCurrentProgress('일시정지');
  }

  Future<void> seekRelative(Duration offset) async {
    final target = _position + offset;
    await seek(target.isNegative ? Duration.zero : target);
  }

  Future<void> seek(Duration position) async {
    await _audioHandler.seek(position);
  }

  // ──────────────────────────────
  // Speed
  // ──────────────────────────────
  Future<void> setSpeed(double speed) async {
    _currentSpeed = speed;
    await _audioHandler.setSpeed(speed);

    // iOS Now Playing Info에 speed 반영
    if (_currentMediaItem != null) {
      final updated = _currentMediaItem!.copyWith(
        extras: {...?_currentMediaItem!.extras, "speed": speed},
      );
      _currentMediaItem = updated;
      _audioHandler.mediaItem.add(updated);
    }

    notifyListeners();
  }

  // ──────────────────────────────
  // Duration injection
  // ──────────────────────────────
  void setExternalDuration(Duration duration) {
    _duration = duration;

    if (_currentMediaItem != null) {
      final updated = _currentMediaItem!.copyWith(duration: duration);
      _currentMediaItem = updated;
      _audioHandler.mediaItem.add(updated);
    }

    notifyListeners();
  }

  // ──────────────────────────────
  // Load Source + MediaItem 설정
  // ──────────────────────────────
  Future<void> loadSource(String url, {MediaItem? mediaItem}) async {
    // 이전 팟캐스트 재생 기록 저장
    if (_currentMediaItem != null) {
      await _saveCurrentProgress('다른 팟캐스트 재생');
    }

    // 새 팟캐스트 로드
    if (mediaItem != null) {
      _currentMediaItem = mediaItem;
      _audioHandler.mediaItem.add(mediaItem);
    }

    await _audioHandler.setSource(url, mediaItem: mediaItem);

    // 실패한 재생 기록 재시도
    await _playingHistoryService.retryFailedRequests();
  }

  // ──────────────────────────────
  // 재생 기록 저장
  // ──────────────────────────────

  /// 현재 재생 위치 저장
  Future<void> _saveCurrentProgress(String reason) async {
    if (_currentMediaItem == null) return;

    final hearitId = _extractHearitId(_currentMediaItem!);
    if (hearitId == null) {
      debugPrint('⚠️ MediaItem에 hearitId가 없습니다.');
      return;
    }

    final lastPlayTime = _position.inMilliseconds;

    await _playingHistoryService.savePlayingHistory(
      hearitId: hearitId,
      lastPlayTime: lastPlayTime,
      reason: reason,
    );
  }

  /// MediaItem extras에서 hearitId 추출
  int? _extractHearitId(MediaItem mediaItem) {
    final id = mediaItem.extras?['hearitId'];
    if (id is int) return id;
    if (id is String) return int.tryParse(id);
    return null;
  }

  /// 앱 생명주기: 백그라운드 진입
  Future<void> saveOnAppPaused() async {
    await _saveCurrentProgress('앱 백그라운드');
  }

  /// 앱 생명주기: 앱 종료
  Future<void> saveOnAppDetached() async {
    await _saveCurrentProgress('앱 종료');
  }

  // ──────────────────────────────
  // Dispose
  // ──────────────────────────────
  @override
  void dispose() {
    _durationSub.cancel();
    _positionSub.cancel();
    _playerStateSub.cancel();
    _audioHandler.disposeHandler();
    super.dispose();
  }
}
