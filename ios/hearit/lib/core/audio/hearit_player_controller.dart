import 'dart:async';

import 'package:audio_service/audio_service.dart';
import 'package:flutter/material.dart';
import 'package:just_audio/just_audio.dart';

import 'audio_handler.dart';

class HearitPlayerController extends ChangeNotifier {
  HearitPlayerController({double initialSpeed = 1.0}) {
    _audioHandler = LocalAudioHandler();
    _audioHandler.setSpeed(initialSpeed);
    _currentSpeed = initialSpeed;
    _durationSub = _audioHandler.durationStream.listen((duration) {
      _duration = duration ?? Duration.zero;
      notifyListeners();
    });
    _positionSub = _audioHandler.positionStream.listen((position) {
      _position = position;
      notifyListeners();
    });
    _playerStateSub = _audioHandler.playerStateStream.listen((state) {
      _latestState = state;
      notifyListeners();
    });
  }

  late final LocalAudioHandler _audioHandler;
  Duration _duration = Duration.zero;
  Duration _position = Duration.zero;
  PlayerState? _latestState;
  double _currentSpeed = 1.0;

  Duration get duration => _duration;
  Duration get position => _position;
  bool get isPlaying => _latestState?.playing ?? false;
  bool get isBuffering {
    final processing = _latestState?.processingState;
    return processing == ProcessingState.loading ||
        processing == ProcessingState.buffering;
  }

  double get currentSpeed => _currentSpeed;

  late final StreamSubscription<Duration?> _durationSub;
  late final StreamSubscription<Duration> _positionSub;
  late final StreamSubscription<PlayerState> _playerStateSub;

  Future<void> togglePlayback() async {
    if (isPlaying) {
      await _audioHandler.pause();
    } else {
      await _audioHandler.play();
    }
  }

  Future<void> seekRelative(Duration offset) async {
    final target = _position + offset;
    await seek(target.isNegative ? Duration.zero : target);
  }

  Future<void> seek(Duration position) async {
    await _audioHandler.seek(position);
  }

  Future<void> setSpeed(double speed) async {
    _currentSpeed = speed;
    await _audioHandler.setSpeed(speed);
    notifyListeners();
  }

  /// Allows externally provided durations (e.g., metadata) to update UI while
  /// using a static/local audio source.
  void setExternalDuration(Duration duration) {
    _duration = duration;
    notifyListeners();
  }

  Future<void> loadSource(String url) async {
    await _audioHandler.setSource(url);
  }

  @override
  void dispose() {
    _durationSub.cancel();
    _positionSub.cancel();
    _playerStateSub.cancel();
    _audioHandler.disposeHandler();
    super.dispose();
  }
}
