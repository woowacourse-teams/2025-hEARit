import 'dart:io';
import 'dart:ui' as ui;

import 'package:audio_service/audio_service.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:path_provider/path_provider.dart';

import '../../core/audio/hearit_player_controller.dart';
import 'detail_repository.dart';
import 'hearit_detail.dart';

class ScriptLine {
  const ScriptLine({
    required this.id,
    required this.start,
    required this.end,
    required this.text,
  });

  final int id;
  final Duration start;
  final Duration end;
  final String text;

  factory ScriptLine.fromJson(Map<String, dynamic> json) {
    return ScriptLine(
      id: json['id'] as int,
      start: Duration(milliseconds: json['start'] as int),
      end: Duration(milliseconds: json['end'] as int),
      text: json['text'] as String,
    );
  }
}

class HearitDetailViewModel extends ChangeNotifier {
  HearitDetailViewModel({
    required HearitDetail detail,
    required HearitPlayerController playerController,
    DetailRepository? repository,
  }) : _detail = detail,
       _playerController = playerController,
       _bookmarked = detail.isBookmarked,
       _repository = repository ?? DetailRepository(),
       _resumePosition = detail.lastPlayTime;

  HearitDetail get detail => _detail;
  HearitDetail _detail;
  final HearitPlayerController _playerController;
  final DetailRepository _repository;
  HearitPlayerController get playerController => _playerController;
  Duration? _resumePosition;
  static final Map<int, Future<Uri>> _artworkUriFutures = {};

  final List<double> _speedOptions = [
    0.5,
    0.75,
    1.0,
    1.25,
    1.5,
    1.75,
    2.0,
  ];
  int _speedIndex = 2;
  bool _bookmarked;
  List<ScriptLine> _scripts = [];
  bool _initialLoading = true;

  bool get isBookmarked => _bookmarked;
  String get speedLabel => '${_formatSpeedLabel(_currentSpeed)}x';
  double get _currentSpeed => _speedOptions[_speedIndex];
  double get currentSpeed => _currentSpeed;
  List<double> get speedOptions => List.unmodifiable(_speedOptions);
  List<ScriptLine> get scripts => _scripts;
  bool get isInitialLoading => _initialLoading;

  Future<void> loadAll() async {
    _initialLoading = true;
    notifyListeners();
    await loadDetail();

    // Kick off audio & script loads concurrently; render UI as soon as detail is ready.
    _initialLoading = false;
    notifyListeners();

    await Future.wait([
      _loadAudio(),
      _loadScripts(),
    ]);
    notifyListeners();
  }

  Future<void> loadDetail() async {
    try {
      final latest = await _repository.fetchDetail(_detail.id);
      _detail = latest;
      _resumePosition ??= latest.lastPlayTime;
      _bookmarked = latest.isBookmarked;
      _playerController.setExternalDuration(latest.playTime);
      notifyListeners();
    } catch (_) {
      // No-op on failure; keep existing data.
    }
  }

  Future<void> _loadAudio() async {
    try {
      final url = await _repository.fetchOriginalAudioUrl(_detail.id);
      if (url != null && url.isNotEmpty) {
        final artUri = await _resolveArtworkUri(_detail.category.color);
        await _playerController.loadSource(
          url,
          mediaItem: MediaItem(
            id: 'hearit-${_detail.id}',
            title: _detail.title,
            album: _detail.category.name,
            artist: _detail.category.name,
            duration: _detail.playTime,
            artUri: artUri,
          ),
        );
        final resumePosition = _resumePosition ?? _detail.lastPlayTime;
        if (resumePosition != null && resumePosition > Duration.zero) {
          await _playerController.seek(resumePosition);
        }
        await _playerController.play();
      }
    } catch (_) {
      // ignore audio load errors for now
    }
  }

  Future<void> _loadScripts() async {
    try {
      _scripts = await _repository.fetchScripts(_detail.id);
    } catch (_) {
      _scripts = [];
    }
    notifyListeners();
  }

  Future<Uri> _resolveArtworkUri(Color accentColor) {
    return _artworkUriFutures.putIfAbsent(
      accentColor.value,
      () => _loadArtworkUri(accentColor),
    );
  }

  Future<Uri> _loadArtworkUri(Color accentColor) async {
    try {
      final bytes = await rootBundle.load('assets/images/backgroud_LP.png');
      final codec = await ui.instantiateImageCodec(bytes.buffer.asUint8List());
      final frame = await codec.getNextFrame();
      final baseImage = frame.image;

      final recorder = ui.PictureRecorder();
      final canvas = ui.Canvas(recorder);
      final size = Size(baseImage.width.toDouble(), baseImage.height.toDouble());
      final rect = Offset.zero & size;

      // Fill background with category color, then paint the LP graphic on top to keep its original colors.
      canvas.drawRect(rect, Paint()..color = accentColor);
      canvas.drawImageRect(
        baseImage,
        Rect.fromLTWH(0, 0, size.width, size.height),
        rect,
        Paint(),
      );

      final picture = recorder.endRecording();
      final tinted = await picture.toImage(baseImage.width, baseImage.height);
      final byteData = await tinted.toByteData(format: ui.ImageByteFormat.png);
      if (byteData == null) {
        return _copyFallbackArtwork();
      }

      final dir = await getTemporaryDirectory();
      final hexColor = accentColor.value.toRadixString(16).padLeft(8, '0');
      final file = File('${dir.path}/detail_LP_$hexColor.png');
      await file.writeAsBytes(byteData.buffer.asUint8List(), flush: true);
      return Uri.file(file.path);
    } catch (_) {
      return _copyFallbackArtwork();
    }
  }

  Future<Uri> _copyFallbackArtwork() async {
    final bytes = await rootBundle.load('assets/images/detail_LP.png');
    final dir = await getTemporaryDirectory();
    final file = File('${dir.path}/detail_LP.png');
    await file.writeAsBytes(bytes.buffer.asUint8List(), flush: true);
    return Uri.file(file.path);
  }

  void toggleBookmark() {
    _bookmarked = !_bookmarked;
    notifyListeners();
  }

  Future<void> seekRelative(Duration offset) =>
      _playerController.seekRelative(offset);

  Future<void> togglePlayback() => _playerController.togglePlayback();

  Future<void> setSpeed(double speed) async {
    final int index = _speedOptions.indexWhere((value) => value == speed);
    if (index == -1) return;
    _speedIndex = index;
    await _playerController.setSpeed(_currentSpeed);
    notifyListeners();
  }

  String _formatSpeedLabel(double speed) {
    final int hundred = (speed * 100).round();
    final int remainder = hundred % 100;
    if (remainder == 0 || remainder == 50) {
      return speed.toStringAsFixed(1);
    }
    return speed.toStringAsFixed(2);
  }

  String formatDuration(Duration duration) {
    final minutes = duration.inMinutes.remainder(60).toString().padLeft(2, '0');
    final seconds = duration.inSeconds.remainder(60).toString().padLeft(2, '0');
    final hours = duration.inHours;
    if (hours > 0) {
      return '${hours.toString().padLeft(2, '0')}:$minutes:$seconds';
    }
    return '$minutes:$seconds';
  }

  String formatDate(DateTime date) {
    final String month = date.month.toString().padLeft(2, '0');
    final String day = date.day.toString().padLeft(2, '0');
    return '${date.year}.$month.$day';
  }

  @override
  void dispose() {
    super.dispose();
  }
}
