import 'package:flutter/material.dart';

class SearchHearit {
  const SearchHearit({
    required this.id,
    required this.title,
    required this.playTimeSeconds,
    required this.lastPlayTimeSeconds,
    required this.isFinished,
    required this.keywords,
  });

  final int id;
  final String title;
  final int playTimeSeconds;
  final int? lastPlayTimeSeconds;
  final bool? isFinished;
  final List<String> keywords;

  Duration get playTime => Duration(seconds: playTimeSeconds);

  Duration? get lastPlayTime =>
      lastPlayTimeSeconds == null ? null : Duration(seconds: lastPlayTimeSeconds!);

  double? get progress {
    if (playTimeSeconds <= 0 || lastPlayTimeSeconds == null) return null;
    return (lastPlayTimeSeconds! / playTimeSeconds).clamp(0, 1);
  }

  String get formattedPlayTime {
    final minutes = playTime.inMinutes;
    final seconds = playTime.inSeconds % 60;
    return '${minutes.toString().padLeft(2, '0')}:${seconds.toString().padLeft(2, '0')}';
  }
}

class SearchCategory {
  const SearchCategory({
    required this.id,
    required this.name,
    required this.color,
  });

  final int id;
  final String name;
  final Color color;
}
