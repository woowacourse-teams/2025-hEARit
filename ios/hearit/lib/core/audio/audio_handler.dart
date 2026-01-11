import 'package:audio_service/audio_service.dart';
import 'package:audio_session/audio_session.dart';
import 'package:just_audio/just_audio.dart';

class LocalAudioHandler extends BaseAudioHandler with SeekHandler {
  LocalAudioHandler() {
    _initializing = _init();
  }

  final AudioPlayer _player = AudioPlayer();
  late final Future<void> _initializing;

  Stream<Duration?> get durationStream => _player.durationStream;
  Stream<Duration> get positionStream => _player.positionStream;
  Stream<PlayerState> get playerStateStream => _player.playerStateStream;

  Future<Duration> get currentPosition async {
    await _initializing;
    return _player.position;
  }

  /// 오디오 소스가 로드되었는지 확인
  bool get hasAudioSource {
    final processingState = _player.processingState;
    // idle = 소스 없음, 나머지는 소스가 로드됨 (loading, buffering, ready, completed)
    return processingState != ProcessingState.idle;
  }

  Future<void> _init() async {
    final session = await AudioSession.instance;
    await session.configure(const AudioSessionConfiguration.music());
    // No default source; wait for explicit load.
    _player.playerStateStream.listen((state) {
      playbackState.add(
        playbackState.value.copyWith(
          controls: [
            MediaControl.rewind,
            state.playing ? MediaControl.pause : MediaControl.play,
            MediaControl.fastForward,
            MediaControl.stop,
          ],
          systemActions: const {
            MediaAction.seek,
            MediaAction.seekForward,
            MediaAction.seekBackward,
          },
          androidCompactActionIndices: const [0, 1, 2],
          processingState: _mapProcessingState(state.processingState),
          playing: state.playing,
          updatePosition: _player.position,
          bufferedPosition: _player.bufferedPosition,
          speed: _player.speed,
        ),
      );
    });
  }

  AudioProcessingState _mapProcessingState(ProcessingState state) {
    switch (state) {
      case ProcessingState.idle:
        return AudioProcessingState.idle;
      case ProcessingState.loading:
        return AudioProcessingState.loading;
      case ProcessingState.buffering:
        return AudioProcessingState.buffering;
      case ProcessingState.ready:
        return AudioProcessingState.ready;
      case ProcessingState.completed:
        return AudioProcessingState.completed;
    }
  }

  @override
  Future<void> play() async {
    await _initializing;
    return _player.play();
  }

  @override
  Future<void> pause() async {
    await _initializing;
    return _player.pause();
  }

  @override
  Future<void> stop() async {
    await _initializing;
    return _player.stop();
  }

  @override
  Future<void> seek(Duration position) async {
    await _initializing;
    return _player.seek(position);
  }

  @override
  Future<void> setSpeed(double speed) async {
    await _initializing;
    await _player.setSpeed(speed);
    playbackState.add(playbackState.value.copyWith(speed: speed));
  }

  Future<void> setSource(String url, {MediaItem? mediaItem}) async {
    await _initializing;
    await _player.setUrl(url);

    if (mediaItem != null) {
      this.mediaItem.add(mediaItem);
    }
  }

  Future<void> updateMediaItem(MediaItem updated) async {
    mediaItem.add(updated);
  }

  Future<void> disposeHandler() async {
    await _player.dispose();
  }
}
