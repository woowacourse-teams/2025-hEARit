import 'package:flutter/material.dart';

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

  final List<double> _speedOptions = [0.75, 1.0, 1.25, 1.5];
  int _speedIndex = 1;
  bool _bookmarked;
  List<ScriptLine> _scripts = [];
  bool _initialLoading = true;

  bool get isBookmarked => _bookmarked;
  String get speedLabel => '${_currentSpeed.toStringAsFixed(1)}x';
  double get _currentSpeed => _speedOptions[_speedIndex];
  List<ScriptLine> get scripts => _scripts;
  bool get isInitialLoading => _initialLoading;

  Future<void> loadAll() async {
    _initialLoading = true;
    notifyListeners();
    await loadDetail();
    await _loadAudio();
    await _loadScripts();
    _initialLoading = false;
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
        await _playerController.loadSource(url);
        final resumePosition = _resumePosition ?? _detail.lastPlayTime;
        if (resumePosition != null && resumePosition > Duration.zero) {
          await _playerController.seek(resumePosition);
        }
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

  void toggleBookmark() {
    _bookmarked = !_bookmarked;
    notifyListeners();
  }

  Future<void> seekRelative(Duration offset) =>
      _playerController.seekRelative(offset);

  Future<void> togglePlayback() => _playerController.togglePlayback();

  Future<void> cycleSpeed() async {
    _speedIndex = (_speedIndex + 1) % _speedOptions.length;
    await _playerController.setSpeed(_currentSpeed);
    notifyListeners();
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
