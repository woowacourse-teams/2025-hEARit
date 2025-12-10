import 'dart:async';

import 'package:audio_service/audio_service.dart';
import 'package:flutter/material.dart';
import 'package:just_audio/just_audio.dart';

import 'audio_handler.dart';

class HearitPlayerController extends ChangeNotifier {
  HearitPlayerController({
    required LocalAudioHandler audioHandler,
    double initialSpeed = 1.0,
  }) : _audioHandler = audioHandler {
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
      _latestState = state;
      notifyListeners();
    });
  }

  late final LocalAudioHandler _audioHandler;

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
  // Controls
  // ──────────────────────────────
  Future<void> play() async => _audioHandler.play();

  Future<void> togglePlayback() async {
    if (isPlaying) {
      await _audioHandler.pause();
    } else {
      await _audioHandler.play();
    }
  }

  Future<void> pause() async => _audioHandler.pause();

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
    if (mediaItem != null) {
      _currentMediaItem = mediaItem;
      _audioHandler.mediaItem.add(mediaItem);
    }

    await _audioHandler.setSource(url, mediaItem: mediaItem);
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
