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
import '../storage/playlist_storage.dart';
import 'audio_handler.dart';
import 'playing_history_service.dart';

class HearitPlayerController extends ChangeNotifier {
  HearitPlayerController({
    required LocalAudioHandler audioHandler,
    PlayingHistoryService? playingHistoryService,
    LibraryRepository? libraryRepository,
    PlaylistStorage? playlistStorage,
    double initialSpeed = 1.0,
  }) : _audioHandler = audioHandler,
       _playingHistoryService =
           playingHistoryService ?? PlayingHistoryService(),
       _libraryRepository = libraryRepository ?? LibraryRepository(),
       _playlistStorage = playlistStorage ?? PlaylistStorage() {
    _currentSpeed = initialSpeed;

    _audioHandler.setSpeed(initialSpeed);

    // 저장된 플레이리스트 로드
    _loadSavedPlaylist();

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

  Duration _duration = Duration.zero;
  Duration _position = Duration.zero;
  PlayerState? _latestState;
  double _currentSpeed = 1.0;

  MediaItem? _currentMediaItem;

  // 플레이리스트 관련 상태
  List<PlaylistItem> _playlist = [];
  int _currentPlaylistIndex = -1; // -1 = 플레이리스트 없음
  bool _isSinglePlayMode = false; // 단일 재생 모드 (상세 화면에서 자동 재생 방지)

  // 탐색 화면 컨텍스트 추적 (백그라운드 일시정지 판단용)
  bool _isPlayingExplorePreview = false;

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
    }

    // _currentPlaylistIndex는 건드리지 않음 (호출자가 이미 설정)

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
