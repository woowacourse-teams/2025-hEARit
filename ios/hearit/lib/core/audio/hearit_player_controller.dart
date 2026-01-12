import 'dart:async';
import 'dart:io';
import 'dart:ui' as ui;

import 'package:audio_service/audio_service.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:just_audio/just_audio.dart';
import 'package:path_provider/path_provider.dart';

import '../../features/library/library_repository.dart';
import '../../features/library/playlist_models.dart';
import '../storage/player_state_storage.dart';
import '../storage/playlist_storage.dart';
import 'audio_handler.dart';
import 'playing_history_service.dart';

/// 탐색 화면 진입 전 상태 저장용
class _SavedPlaybackState {
  const _SavedPlaybackState({
    required this.mediaItem,
    required this.audioUrl,
    required this.position,
    required this.wasPlaying,
    required this.playlist,
    required this.playlistIndex,
    required this.isSinglePlayMode,
    required this.currentSpeed,
  });

  final MediaItem? mediaItem;
  final String? audioUrl;
  final Duration position;
  final bool wasPlaying;
  final List<PlaylistItem> playlist;
  final int playlistIndex;
  final bool isSinglePlayMode;
  final double currentSpeed;
}

class HearitPlayerController extends ChangeNotifier {
  HearitPlayerController({
    required LocalAudioHandler audioHandler,
    PlayingHistoryService? playingHistoryService,
    LibraryRepository? libraryRepository,
    PlaylistStorage? playlistStorage,
    PlayerStateStorage? playerStateStorage,
    double initialSpeed = 1.0,
  }) : _audioHandler = audioHandler,
       _playingHistoryService =
           playingHistoryService ?? PlayingHistoryService(),
       _libraryRepository = libraryRepository ?? LibraryRepository(),
       _playlistStorage = playlistStorage ?? PlaylistStorage(),
       _playerStateStorage = playerStateStorage ?? PlayerStateStorage() {
    _currentSpeed = initialSpeed;

    _audioHandler.setSpeed(initialSpeed);

    // 저장된 플레이리스트 로드
    _loadSavedPlaylist();

    // 저장된 재생바 상태 복원
    _restorePlayerState();

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
  final LibraryRepository _libraryRepository;
  late final PlaylistStorage _playlistStorage;
  late final PlayerStateStorage _playerStateStorage;

  Duration _duration = Duration.zero;
  Duration _position = Duration.zero;
  PlayerState? _latestState;
  double _currentSpeed = 1.0;

  MediaItem? _currentMediaItem;
  String? _currentAudioUrl; // 현재 오디오 URL 추적

  // 플레이리스트 관련 상태
  List<PlaylistItem> _playlist = [];
  int _currentPlaylistIndex = -1; // -1 = 플레이리스트 없음
  bool _isSinglePlayMode = false; // 단일 재생 모드 (상세 화면에서 자동 재생 방지)

  // 탐색 화면 컨텍스트 추적 (백그라운드 일시정지 판단용)
  bool _isPlayingExplorePreview = false;

  // 탐색 화면 진입 전 상태 저장
  _SavedPlaybackState? _beforeExploreState;

  // 아트워크 캐시 (색상별로 캐싱)
  static final Map<int, Future<Uri>> _artworkUriFutures = {};

  Duration get duration => _duration;
  Duration get position => _position;
  double get currentSpeed => _currentSpeed;
  MediaItem? get currentMediaItem => _currentMediaItem;

  // 플레이리스트 getters
  List<PlaylistItem> get playlist => _playlist;
  int get currentPlaylistIndex => _currentPlaylistIndex;
  bool get isPlayingFromPlaylist =>
      _currentPlaylistIndex >= 0 && _playlist.isNotEmpty;
  bool get hasNextInPlaylist =>
      isPlayingFromPlaylist && _currentPlaylistIndex < _playlist.length - 1;
  bool get hasPreviousInPlaylist =>
      isPlayingFromPlaylist && _currentPlaylistIndex > 0;

  bool get isPlaying => _latestState?.playing ?? false;

  // 탐색 화면 미리듣기 재생 중인지 여부
  bool get isPlayingExplorePreview => _isPlayingExplorePreview;

  bool get isBuffering {
    final s = _latestState?.processingState;
    return s == ProcessingState.loading || s == ProcessingState.buffering;
  }

  bool get isCompleted =>
      _latestState?.processingState == ProcessingState.completed;

  // 오디오 소스가 로드되었는지 확인
  bool get hasAudioSource => _audioHandler.hasAudioSource;

  // 오디오 로딩 중인지 확인 (복원된 상태에서 API 호출 중)
  bool _isLoadingAudio = false;
  bool get isLoadingAudio => _isLoadingAudio;

  late final StreamSubscription<Duration?> _durationSub;
  late final StreamSubscription<Duration> _positionSub;
  late final StreamSubscription<PlayerState> _playerStateSub;

  // ──────────────────────────────
  // 저장된 플레이리스트 로드
  // ──────────────────────────────
  Future<void> _loadSavedPlaylist() async {
    final savedPlaylist = await _playlistStorage.loadPlaylist();
    if (savedPlaylist != null && savedPlaylist.isNotEmpty) {
      _playlist = savedPlaylist;
      debugPrint('🎵 저장된 플레이리스트 로드: ${savedPlaylist.length}개 항목');
      notifyListeners();
    }
  }

  // ──────────────────────────────
  // 저장된 재생바 상태 복원
  // ──────────────────────────────
  Future<void> _restorePlayerState() async {
    final savedState = await _playerStateStorage.loadPlayerState();

    if (savedState == null) {
      debugPrint('📭 복원할 재생바 상태 없음');
      return;
    }

    // 만료된 상태는 무시
    if (savedState.isExpired) {
      final age = DateTime.now().difference(savedState.savedAt);
      debugPrint('⏰ 재생바 상태 만료 (${age.inDays}일 경과)');
      await _playerStateStorage.clearPlayerState();
      return;
    }

    debugPrint('🎵 재생바 상태 복원: ${savedState.title}');

    // MediaItem 복원 (오디오 소스는 로드하지 않음)
    _currentMediaItem = MediaItem(
      id: 'hearit-${savedState.hearitId}',
      title: savedState.title,
      album: savedState.album,
      artist: savedState.artist,
      duration: Duration(seconds: savedState.durationSeconds),
      artUri: null, // 필요 시 나중에 로드
      extras: {'hearitId': savedState.hearitId},
    );

    // Duration 복원
    _duration = Duration(seconds: savedState.durationSeconds);

    // Position 복원 (UI에 표시용)
    _position = Duration(milliseconds: savedState.lastPositionMs);

    // 플레이리스트 인덱스 복원 (플레이리스트가 로드된 경우)
    if (savedState.playlistIndex != null && _playlist.isNotEmpty) {
      // hearitId로 현재 플레이리스트에서 인덱스 재계산
      final savedHearitId = savedState.hearitId;
      final newIndex = _playlist.indexWhere(
        (item) => item.hearitId == savedHearitId,
      );

      if (newIndex >= 0) {
        _currentPlaylistIndex = newIndex;
        debugPrint('📂 플레이리스트 인덱스 복원: $_currentPlaylistIndex');
      } else {
        // 플레이리스트에서 제거된 항목 → 단일 재생으로 간주
        _currentPlaylistIndex = -1;
        debugPrint('⚠️ 플레이리스트에서 항목 제거됨, 단일 재생으로 전환');
      }
    }

    // MediaItem을 AudioHandler에도 설정 (알림/잠금화면 표시용)
    _audioHandler.mediaItem.add(_currentMediaItem);

    notifyListeners();
  }

  // ──────────────────────────────
  // PlayerState 변화 처리 (재생 기록 저장 포함)
  // ──────────────────────────────
  void _handlePlayerStateChange(PlayerState state) {
    final prevState = _latestState;
    _latestState = state;

    // 1. 재생 완료 감지
    if (state.processingState == ProcessingState.completed) {
      _saveCurrentProgress('재생 완료');

      // 플레이리스트 재생 중이고 단일 재생 모드가 아닐 때만 자동으로 다음 곡 재생
      if (hasNextInPlaylist && !_isSinglePlayMode) {
        Future.delayed(const Duration(milliseconds: 500), () {
          playNext();
        });
      } else if (_isSinglePlayMode) {
        // 단일 재생 모드일 때는 명시적으로 일시정지 처리 (UI 아이콘 업데이트)
        Future.delayed(const Duration(milliseconds: 100), () {
          pause();
        });
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
      // 오디오 소스가 없으면 먼저 로드
      if (!hasAudioSource) {
        final success = await ensureAudioLoaded();
        if (!success) {
          debugPrint('❌ 오디오 로드 실패 - 재생 불가');
          return;
        }
      }
      await _audioHandler.play();
    }
  }

  Future<void> pause() async {
    await _audioHandler.pause();
    // 일시정지 시 재생 기록 저장
    await _saveCurrentProgress('일시정지');
    // 재생바 상태 저장
    await _savePlayerState('일시정지');
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

  /// 상세 화면에서 재생 시 사용 (플레이리스트 컨텍스트 선택적 유지)
  Future<void> loadSource(
    String url, {
    MediaItem? mediaItem,
    bool keepPlaylistContext = false, // 플레이리스트 컨텍스트 유지 여부
    bool singlePlayMode = false, // 단일 재생 모드 (자동 재생 방지)
    bool isExplorePreview = false, // 탐색 화면 미리듣기 여부
  }) async {
    // 이전 팟캐스트 재생 기록 저장
    if (_currentMediaItem != null) {
      await _saveCurrentProgress('다른 팟캐스트 재생');
      await _savePlayerState('다른 팟캐스트 재생');
    }

    // 플레이리스트 컨텍스트 유지 여부에 따라 인덱스 처리
    if (!keepPlaylistContext) {
      // 기존 동작: 상세 화면에서 재생 시 플레이리스트 인덱스 해제
      _currentPlaylistIndex = -1;
    }
    // keepPlaylistContext == true면 _currentPlaylistIndex를 그대로 유지
    // (재생목록에서 진입한 경우 하이라이트 유지)

    // 단일 재생 모드 설정
    _isSinglePlayMode = singlePlayMode;

    // 탐색 화면 미리듣기 여부 설정
    _isPlayingExplorePreview = isExplorePreview;

    // 현재 오디오 URL 저장
    _currentAudioUrl = url;

    // 배속을 기본값(1.0x)으로 초기화
    if (_currentSpeed != 1.0) {
      _currentSpeed = 1.0;
      await _audioHandler.setSpeed(1.0);
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

  /// 플레이리스트 재생 시 사용 (플레이리스트 인덱스 유지)
  Future<void> _loadSourceFromPlaylist(String url, MediaItem mediaItem) async {
    // 플레이리스트 재생 모드로 전환 (자동 재생 허용)
    _isSinglePlayMode = false;

    // 탐색 화면 컨텍스트 해제 (플레이리스트는 풀 콘텐츠)
    _isPlayingExplorePreview = false;

    // 이전 팟캐스트 재생 기록 저장
    if (_currentMediaItem != null) {
      await _saveCurrentProgress('다른 팟캐스트 재생');
      await _savePlayerState('다른 팟캐스트 재생');
    }

    // _currentPlaylistIndex는 건드리지 않음 (호출자가 이미 설정)

    // 현재 오디오 URL 저장
    _currentAudioUrl = url;

    // 배속을 기본값(1.0x)으로 초기화
    if (_currentSpeed != 1.0) {
      _currentSpeed = 1.0;
      await _audioHandler.setSpeed(1.0);
    }

    // 새 팟캐스트 로드
    _currentMediaItem = mediaItem;
    _audioHandler.mediaItem.add(mediaItem);

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

  /// 현재 재생 상태를 로컬에 저장 (재생바 복원용)
  Future<void> _savePlayerState(String reason) async {
    if (_currentMediaItem == null) {
      // 재생 중인 항목이 없으면 저장된 상태 삭제
      await _playerStateStorage.clearPlayerState();
      debugPrint('🗑️ 재생바 상태 삭제: $reason');
      return;
    }

    final hearitId = _extractHearitId(_currentMediaItem!);
    if (hearitId == null) {
      debugPrint('⚠️ MediaItem에 hearitId가 없습니다.');
      return;
    }

    final state = PlayerStateModel(
      hearitId: hearitId,
      title: _currentMediaItem!.title,
      album: _currentMediaItem!.album ?? '',
      artist: _currentMediaItem!.artist ?? '',
      durationSeconds: _duration.inSeconds,
      lastPositionMs: _position.inMilliseconds,
      playlistIndex: _currentPlaylistIndex >= 0 ? _currentPlaylistIndex : null,
      savedAt: DateTime.now(),
    );

    await _playerStateStorage.savePlayerState(state);
    debugPrint(
      '💾 재생바 상태 저장 완료: ${_currentMediaItem!.title} (위치: ${_position.inMilliseconds}ms)',
    );
    debugPrint('💾 재생바 상태 저장: $reason');
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
    await _savePlayerState('앱 백그라운드');
  }

  /// 앱 생명주기: 앱 종료
  Future<void> saveOnAppDetached() async {
    await _saveCurrentProgress('앱 종료');
    await _savePlayerState('앱 종료');
  }

  // ──────────────────────────────
  // 오디오 소스 로드 (복원된 상태에서)
  // ──────────────────────────────

  /// 오디오 소스가 없으면 hearitId로 API 호출하여 로드
  Future<bool> ensureAudioLoaded() async {
    // 이미 오디오 소스가 있으면 성공
    if (hasAudioSource) {
      return true;
    }

    // MediaItem이 없으면 실패
    if (_currentMediaItem == null) {
      debugPrint('⚠️ MediaItem이 없어서 오디오 로드 불가');
      return false;
    }

    // 이미 로딩 중이면 대기
    if (_isLoadingAudio) {
      debugPrint('⏳ 이미 오디오 로딩 중...');
      return false;
    }

    final hearitId = _extractHearitId(_currentMediaItem!);
    if (hearitId == null) {
      debugPrint('⚠️ hearitId를 찾을 수 없음');
      return false;
    }

    try {
      _isLoadingAudio = true;
      notifyListeners();

      debugPrint('🔄 복원된 상태 - 오디오 URL 가져오는 중... (hearitId: $hearitId)');

      // API로 오디오 URL 가져오기
      final url = await _libraryRepository.fetchOriginalAudioUrl(hearitId);

      if (url == null || url.isEmpty) {
        debugPrint('❌ 오디오 URL을 가져오지 못함');
        _isLoadingAudio = false;
        notifyListeners();
        return false;
      }

      debugPrint('✅ 오디오 URL 가져오기 성공: $url');

      // 저장된 위치 백업
      final savedPosition = _position;

      // 오디오 소스 로드
      await _audioHandler.setSource(url, mediaItem: _currentMediaItem);

      // 저장된 위치로 seek
      if (savedPosition > Duration.zero) {
        await _audioHandler.seek(savedPosition);
      }

      debugPrint('✅ 오디오 로드 완료 (위치: ${savedPosition.inSeconds}초)');

      _isLoadingAudio = false;
      notifyListeners();
      return true;
    } catch (e) {
      debugPrint('❌ 오디오 로드 실패: $e');
      _isLoadingAudio = false;
      notifyListeners();
      return false;
    }
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

    // 로컬 스토리지에 저장 (50개 제한은 PlaylistStorage에서 처리)
    await _playlistStorage.savePlaylist(playlist);
    debugPrint(
      '🎵 플레이리스트 로드 및 저장: ${playlist.length}개 항목, 시작 인덱스: $startIndex',
    );

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

      // 플레이리스트 재생용 로드 (인덱스 유지)
      await _loadSourceFromPlaylist(item.audioUrl!, mediaItem);
      await play();

      notifyListeners();
    } catch (e) {
      debugPrint('❌ 재생 중 오류: $e');
      await playNext(); // 다음 곡 시도
    }
  }

  /// 오디오 원본 URL 가져오기 (API 호출)
  Future<String?> _fetchAudioUrl(int hearitId) async {
    try {
      final url = await _libraryRepository.fetchOriginalAudioUrl(hearitId);
      if (url != null && url.isNotEmpty) {
        debugPrint('✅ 오디오 URL 로드 성공: hearitId=$hearitId');
        return url;
      } else {
        debugPrint('⚠️ 오디오 URL이 비어있음: hearitId=$hearitId');
        return null;
      }
    } catch (e) {
      debugPrint('❌ 오디오 URL 로드 실패: hearitId=$hearitId, error=$e');
      return null;
    }
  }

  /// 카테고리 색상 코드로 아트워크 URI 생성 (로컬 이미지 사용)
  Future<Uri> _resolveArtworkUri(String colorCode) async {
    // 색상 코드를 Color 객체로 변환
    final color = _parseColorCode(colorCode);

    // 캐시된 Future 반환 (동일한 색상은 한 번만 생성)
    return _artworkUriFutures.putIfAbsent(
      color.value,
      () => _loadArtworkUri(color),
    );
  }

  /// 색상 코드를 Color로 변환
  Color _parseColorCode(String hexColor) {
    try {
      final hex = hexColor.replaceAll('#', '');
      return Color(int.parse('FF$hex', radix: 16));
    } catch (e) {
      debugPrint('⚠️ 색상 파싱 실패: $hexColor, 기본 색상 사용');
      return const Color(0xFF6366F1); // 기본 보라색
    }
  }

  /// 로컬 이미지를 색상으로 tinting하여 파일로 저장
  Future<Uri> _loadArtworkUri(Color accentColor) async {
    try {
      // 1. 로컬 이미지 로드
      final bytes = await rootBundle.load('assets/images/backgroud_LP.png');
      final codec = await ui.instantiateImageCodec(bytes.buffer.asUint8List());
      final frame = await codec.getNextFrame();
      final baseImage = frame.image;

      // 2. 캔버스에 색상 배경 + 이미지 그리기
      final recorder = ui.PictureRecorder();
      final canvas = ui.Canvas(recorder);
      final size = Size(
        baseImage.width.toDouble(),
        baseImage.height.toDouble(),
      );
      final rect = Offset.zero & size;

      // 배경을 카테고리 색상으로 채우고, LP 그래픽을 위에 그림
      canvas.drawRect(rect, Paint()..color = accentColor);
      canvas.drawImageRect(
        baseImage,
        Rect.fromLTWH(0, 0, size.width, size.height),
        rect,
        Paint(),
      );

      // 3. 이미지로 변환
      final picture = recorder.endRecording();
      final tinted = await picture.toImage(baseImage.width, baseImage.height);
      final byteData = await tinted.toByteData(format: ui.ImageByteFormat.png);
      if (byteData == null) {
        return _copyFallbackArtwork();
      }

      // 4. 임시 디렉토리에 파일로 저장
      final dir = await getTemporaryDirectory();
      final hexColor = accentColor.value.toRadixString(16).padLeft(8, '0');
      final file = File('${dir.path}/playlist_LP_$hexColor.png');
      await file.writeAsBytes(byteData.buffer.asUint8List(), flush: true);

      debugPrint('✅ 아트워크 생성 완료: ${file.path}');
      return Uri.file(file.path);
    } catch (e) {
      debugPrint('❌ 아트워크 생성 실패: $e, fallback 사용');
      return _copyFallbackArtwork();
    }
  }

  /// Fallback 아트워크 (기본 LP 이미지)
  Future<Uri> _copyFallbackArtwork() async {
    try {
      final bytes = await rootBundle.load('assets/images/detail_LP.png');
      final dir = await getTemporaryDirectory();
      final file = File('${dir.path}/playlist_LP_fallback.png');
      await file.writeAsBytes(bytes.buffer.asUint8List(), flush: true);
      return Uri.file(file.path);
    } catch (e) {
      debugPrint('❌ Fallback 아트워크 생성 실패: $e');
      // 최후의 fallback: assets 경로 반환 (일부 플랫폼에서는 작동하지 않을 수 있음)
      return Uri.parse('asset:///assets/images/detail_LP.png');
    }
  }

  /// 다음 곡 재생
  Future<void> playNext() async {
    if (!hasNextInPlaylist) {
      // 플레이리스트 종료
      debugPrint('🎵 플레이리스트 종료 (마지막 곡)');
      await pause();
      notifyListeners();
      return;
    }

    await _playItemAtIndex(_currentPlaylistIndex + 1);
  }

  /// 이전 곡 재생
  Future<void> playPrevious() async {
    if (!hasPreviousInPlaylist) {
      debugPrint('⚠️ 이전 곡 없음');
      return;
    }

    await _playItemAtIndex(_currentPlaylistIndex - 1);
  }

  /// 플레이리스트의 특정 항목 재생
  Future<void> playPlaylistItem(int index) async {
    if (_playlist.isEmpty) {
      debugPrint('⚠️ 플레이리스트가 비어있습니다.');
      return;
    }

    if (index < 0 || index >= _playlist.length) {
      debugPrint('⚠️ 잘못된 인덱스: $index');
      return;
    }

    _currentPlaylistIndex = index;
    await _playItemAtIndex(index);
  }

  /// 플레이리스트에서 항목 제거 (북마크 삭제 시)
  Future<void> removeFromPlaylist(int hearitId) async {
    if (!isPlayingFromPlaylist) return;

    final index = _playlist.indexWhere((item) => item.hearitId == hearitId);
    if (index == -1) return;

    debugPrint('🗑️ 플레이리스트에서 제거: hearitId=$hearitId, index=$index');

    _playlist.removeAt(index);

    // 현재 재생 중인 곡이 삭제됨
    if (index == _currentPlaylistIndex) {
      // 다음 곡으로 스킵 (마지막 곡이면 종료)
      if (_playlist.isEmpty) {
        await clearPlaylist();
      } else {
        await playNext();
      }
    } else if (index < _currentPlaylistIndex) {
      // 인덱스 조정
      _currentPlaylistIndex--;
    }

    notifyListeners();
  }

  /// 플레이리스트 종료
  Future<void> clearPlaylist() async {
    debugPrint('🎵 플레이리스트 클리어');
    _playlist.clear();
    _currentPlaylistIndex = -1;

    // 로컬 스토리지에서도 삭제
    await _playlistStorage.clearPlaylist();

    notifyListeners();
  }

  // ──────────────────────────────
  // 탐색 화면 상태 저장/복원
  // ──────────────────────────────

  /// 탐색 화면 진입 전 현재 재생 상태 저장
  Future<void> saveStateBeforeExplore() async {
    // 이미 탐색 미리듣기 중이면 중복 저장 방지
    if (_isPlayingExplorePreview) {
      debugPrint('⚠️ 이미 탐색 미리듣기 중 - 상태 저장 스킵');
      return;
    }

    // 현재 상태 스냅샷 저장
    _beforeExploreState = _SavedPlaybackState(
      mediaItem: _currentMediaItem,
      audioUrl: _currentAudioUrl,
      position: _position,
      wasPlaying: isPlaying,
      playlist: List.from(_playlist),
      playlistIndex: _currentPlaylistIndex,
      isSinglePlayMode: _isSinglePlayMode,
      currentSpeed: _currentSpeed,
    );

    debugPrint('💾 탐색 진입 전 상태 저장 완료:');
    debugPrint('   - MediaItem: ${_currentMediaItem?.title}');
    debugPrint('   - Position: ${_position.inSeconds}초');
    debugPrint('   - Playing: $isPlaying');
  }

  /// 탐색 화면을 나간 후 이전 재생 상태 복원
  Future<void> restoreStateAfterExplore() async {
    debugPrint('🔄 [복원] restoreStateAfterExplore() 호출됨');

    // 복원할 상태가 없으면 리턴
    if (_beforeExploreState == null) {
      debugPrint('⚠️ [복원] 복원할 상태 없음 (_beforeExploreState == null)');
      return;
    }

    debugPrint('🔍 [복원] 현재 상태 체크:');
    debugPrint('   - _isPlayingExplorePreview: $_isPlayingExplorePreview');
    debugPrint(
      '   - _beforeExploreState: ${_beforeExploreState?.mediaItem?.title}',
    );

    // ✅ FIX: 탐색 미리듣기 중이었을 때만 복원
    if (_isPlayingExplorePreview) {
      debugPrint('✅ [복원] 탐색 미리듣기였음 - 복원 시작');

      final saved = _beforeExploreState!;
      _beforeExploreState = null; // 복원 후 클리어
      _isPlayingExplorePreview = false; // 탐색 컨텍스트 해제

      debugPrint('🔄 [복원] 저장된 상태:');
      debugPrint('   - MediaItem: ${saved.mediaItem?.title}');
      debugPrint('   - Position: ${saved.position.inSeconds}초');
      debugPrint('   - WasPlaying: ${saved.wasPlaying}');

      // 이전에 재생 중인 것이 없었으면 클리어만
      if (saved.mediaItem == null || saved.audioUrl == null) {
        _currentMediaItem = null;
        _currentAudioUrl = null;
        _playlist = [];
        _currentPlaylistIndex = -1;
        notifyListeners();
        debugPrint('✅ [복원] 이전 재생 없음 - 상태 클리어 완료');
        return;
      }

      // 상태 복원
      _currentMediaItem = saved.mediaItem;
      _currentAudioUrl = saved.audioUrl;
      _playlist = saved.playlist;
      _currentPlaylistIndex = saved.playlistIndex;
      _isSinglePlayMode = saved.isSinglePlayMode;

      // 배속 복원
      if (_currentSpeed != saved.currentSpeed) {
        _currentSpeed = saved.currentSpeed;
        await _audioHandler.setSpeed(saved.currentSpeed);
        debugPrint('⚡ [복원] 배속 복원: ${saved.currentSpeed}x');
      }

      // MediaItem 복원
      _audioHandler.mediaItem.add(saved.mediaItem);
      debugPrint('📱 [복원] MediaItem 복원 완료');

      try {
        // 오디오 소스 복원 (네트워크 요청 발생)
        debugPrint('🌐 [복원] 오디오 소스 로딩 시작: ${saved.audioUrl}');
        await _audioHandler
            .setSource(saved.audioUrl!, mediaItem: saved.mediaItem)
            .timeout(
              const Duration(seconds: 10),
              onTimeout: () {
                debugPrint('⚠️ [복원] 오디오 소스 로딩 타임아웃 (10초)');
                throw TimeoutException('Audio source loading timeout');
              },
            );
        debugPrint('✅ [복원] 오디오 소스 로딩 완료');

        // ✅ FIX: 즉시 UI 업데이트 (스켈레톤 해제)
        debugPrint('🔔 [복원] notifyListeners() 호출 (UI 즉시 업데이트)');
        notifyListeners();
        debugPrint('✅ [복원] UI 업데이트 완료 - 스켈레톤 해제됨!');

        // ✅ FIX: seek와 play를 백그라운드에서 실행 (UI 블로킹 없음)
        debugPrint('🚀 [복원] seek/play 백그라운드 실행 시작');
        Future.microtask(() async {
          try {
            // 위치 복원
            if (saved.position > Duration.zero) {
              debugPrint('⏩ [복원-BG] seek() 시작: ${saved.position.inSeconds}초');
              await _audioHandler.seek(saved.position);
              _position = saved.position;
              debugPrint('✅ [복원-BG] seek() 완료: ${saved.position.inSeconds}초');
            }

            // 재생 상태 복원
            if (saved.wasPlaying) {
              debugPrint('▶️ [복원-BG] play() 시작');
              await _audioHandler.play();
              debugPrint('✅ [복원-BG] play() 완료');
            } else {
              debugPrint('⏸️ [복원-BG] 일시정지 상태 유지');
            }

            debugPrint('🎉 [복원-BG] 백그라운드 복원 완료!');
          } catch (e) {
            debugPrint('⚠️ [복원-BG] 백그라운드 복원 실패: $e');
          }
        });

        debugPrint('✅✅✅ [복원] 메인 복원 완료 (seek/play는 백그라운드 진행 중)');
      } catch (e, stackTrace) {
        debugPrint('❌ [복원] 오디오 복원 실패: $e');
        debugPrint('📍 [복원] 스택 트레이스: $stackTrace');
        // 실패 시에도 UI는 업데이트
        notifyListeners();
        debugPrint('🔔 [복원] 예외 발생했지만 notifyListeners() 호출함');
      }
    } else {
      debugPrint('⚠️ [복원] 탐색 미리듣기가 아니었음 - 복원 불필요');
      debugPrint('   (정상적인 케이스: 탐색에서 미리듣기를 안 했을 때)');
      return;
    }
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
