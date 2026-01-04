import 'dart:async';

import 'package:audio_service/audio_service.dart';
import 'package:flutter/material.dart';
import 'package:just_audio/just_audio.dart';

import '../../features/library/playlist_models.dart';
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

  // 플레이리스트 관련 상태
  List<PlaylistItem> _playlist = [];
  int _currentPlaylistIndex = -1; // -1 = 플레이리스트 없음
  bool _isPlaylistMode = false;

  Duration get duration => _duration;
  Duration get position => _position;
  double get currentSpeed => _currentSpeed;
  MediaItem? get currentMediaItem => _currentMediaItem;

  // 플레이리스트 getters
  List<PlaylistItem> get playlist => _playlist;
  int get currentPlaylistIndex => _currentPlaylistIndex;
  bool get isPlaylistMode => _isPlaylistMode;
  bool get hasNextInPlaylist =>
      _isPlaylistMode && _currentPlaylistIndex < _playlist.length - 1;
  bool get hasPreviousInPlaylist =>
      _isPlaylistMode && _currentPlaylistIndex > 0;

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

      // 플레이리스트 모드: 자동으로 다음 곡 재생
      if (_isPlaylistMode && hasNextInPlaylist) {
        Future.delayed(const Duration(milliseconds: 500), () {
          playNext();
        });
      } else if (_isPlaylistMode) {
        // 마지막 곡 완료: 플레이리스트 종료 (멈춤 상태)
        debugPrint('🎵 플레이리스트 재생 완료');
      }
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
  // 플레이리스트 기능
  // ──────────────────────────────

  /// 플레이리스트 로드 및 첫 곡 재생
  Future<void> loadPlaylist({
    required List<PlaylistItem> playlist,
    int startIndex = 0,
  }) async {
    if (playlist.isEmpty) {
      debugPrint('⚠️ 빈 플레이리스트');
      return;
    }

    _playlist = playlist;
    _currentPlaylistIndex = startIndex;
    _isPlaylistMode = true;

    debugPrint('🎵 플레이리스트 로드: ${playlist.length}개 항목, 시작 인덱스: $startIndex');

    // 첫 곡 재생
    await _playItemAtIndex(startIndex);
  }

  /// 특정 인덱스의 곡 재생
  Future<void> _playItemAtIndex(int index) async {
    if (index < 0 || index >= _playlist.length) {
      debugPrint('⚠️ 잘못된 플레이리스트 인덱스: $index');
      return;
    }

    final item = _playlist[index];
    _currentPlaylistIndex = index;

    debugPrint('🎵 재생 시작: [$index/${_playlist.length - 1}] ${item.title}');

    try {
      // 오디오 URL 가져오기 (캐싱)
      if (!item.hasAudioUrl) {
        final url = await _fetchAudioUrl(item.hearitId);
        if (url == null || url.isEmpty) {
          // 에러 처리: 다음 곡으로 스킵
          debugPrint('❌ 오디오 URL 로드 실패: ${item.title}');
          // TODO: 토스트 메시지 표시 (BuildContext 필요)
          await playNext(); // 재귀 호출
          return;
        }
        item.audioUrl = url;
      }

      // MediaItem 생성
      final artUri = await _resolveArtworkUri(item.categoryColorCode);
      final mediaItem = MediaItem(
        id: 'hearit-${item.hearitId}',
        title: item.title,
        album: item.sourceName,
        artist: item.sourceName,
        duration: Duration(seconds: item.playTimeSeconds),
        artUri: artUri,
        extras: {'hearitId': item.hearitId},
      );

      // 로드 및 재생
      await loadSource(item.audioUrl!, mediaItem: mediaItem);
      await play();

      notifyListeners();
    } catch (e) {
      debugPrint('❌ 재생 중 오류: $e');
      await playNext(); // 다음 곡 시도
    }
  }

  /// 오디오 원본 URL 가져오기 (API 호출)
  Future<String?> _fetchAudioUrl(int hearitId) async {
    // TODO: LibraryRepository의 fetchOriginalAudioUrl 사용
    // 현재는 임시로 null 반환 (실제 구현 시 의존성 주입 필요)
    debugPrint('⚠️ _fetchAudioUrl는 아직 구현되지 않았습니다. hearitId: $hearitId');
    return null;
  }

  /// 카테고리 색상 코드로 아트워크 URI 생성
  Future<Uri> _resolveArtworkUri(String colorCode) async {
    final hexColor = colorCode.replaceAll('#', '');
    return Uri.parse(
      'https://via.placeholder.com/300/$hexColor/FFFFFF?text=hEARit',
    );
  }

  /// 다음 곡 재생
  Future<void> playNext() async {
    if (!_isPlaylistMode || !hasNextInPlaylist) {
      // 플레이리스트 종료
      debugPrint('🎵 플레이리스트 종료 (마지막 곡)');
      _isPlaylistMode = false;
      await pause();
      notifyListeners();
      return;
    }

    await _playItemAtIndex(_currentPlaylistIndex + 1);
  }

  /// 이전 곡 재생
  Future<void> playPrevious() async {
    if (!_isPlaylistMode || !hasPreviousInPlaylist) {
      debugPrint('⚠️ 이전 곡 없음');
      return;
    }

    await _playItemAtIndex(_currentPlaylistIndex - 1);
  }

  /// 플레이리스트의 특정 항목 재생
  Future<void> playPlaylistItem(int index) async {
    if (!_isPlaylistMode) {
      debugPrint('⚠️ 플레이리스트 모드가 아닙니다');
      return;
    }

    await _playItemAtIndex(index);
  }

  /// 플레이리스트에서 항목 제거 (북마크 삭제 시)
  void removeFromPlaylist(int hearitId) {
    if (!_isPlaylistMode) return;

    final index = _playlist.indexWhere((item) => item.hearitId == hearitId);
    if (index == -1) return;

    debugPrint('🗑️ 플레이리스트에서 제거: hearitId=$hearitId, index=$index');

    _playlist.removeAt(index);

    // 현재 재생 중인 곡이 삭제됨
    if (index == _currentPlaylistIndex) {
      // 다음 곡으로 스킵 (마지막 곡이면 종료)
      if (_playlist.isEmpty) {
        clearPlaylist();
      } else {
        playNext();
      }
    } else if (index < _currentPlaylistIndex) {
      // 인덱스 조정
      _currentPlaylistIndex--;
    }

    notifyListeners();
  }

  /// 플레이리스트 종료
  void clearPlaylist() {
    debugPrint('🎵 플레이리스트 클리어');
    _playlist.clear();
    _currentPlaylistIndex = -1;
    _isPlaylistMode = false;
    notifyListeners();
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
