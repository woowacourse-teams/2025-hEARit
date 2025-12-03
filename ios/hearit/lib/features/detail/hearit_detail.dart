import 'package:flutter/material.dart';

import '../../core/theme/app_colors.dart';

class HearitSource {
  const HearitSource({required this.sourceName, required this.sourceUrl});

  final String sourceName;
  final String sourceUrl;

  factory HearitSource.fromJson(Map<String, dynamic> json) => HearitSource(
        sourceName: json['sourceName'] as String? ?? '',
        sourceUrl: (json['sourceUrl'] as String?) ?? '',
      );
}

class HearitKeyword {
  const HearitKeyword({required this.id, required this.name});

  final int id;
  final String name;

  factory HearitKeyword.fromJson(Map<String, dynamic> json) => HearitKeyword(
        id: json['id'] is int ? json['id'] as int : int.parse(json['id'].toString()),
        name: json['name'] as String,
      );
}

class HearitCategory {
  const HearitCategory({
    required this.id,
    required this.name,
    required this.color,
  });

  final int id;
  final String name;
  final Color color;

  factory HearitCategory.fromJson(Map<String, dynamic> json) => HearitCategory(
        id: json['id'] is int ? json['id'] as int : int.parse(json['id'].toString()),
        name: json['name'] as String,
        color: _parseColor(json['colorCode'] as String),
      );
}

class HearitDetail {
  const HearitDetail({
    required this.id,
    required this.title,
    required this.summary,
    required this.sources,
    required this.playTime,
    required this.lastPlayTime,
    required this.createdAt,
    required this.isBookmarked,
    required this.bookmarkId,
    required this.category,
    required this.keywords,
  });

  final int id;
  final String title;
  final String summary;
  final List<HearitSource> sources;
  final Duration playTime;
  final Duration? lastPlayTime;
  final DateTime createdAt;
  final bool isBookmarked;
  final int? bookmarkId;
  final HearitCategory category;
  final List<HearitKeyword> keywords;

  Color get accentColor => category.color;

  factory HearitDetail.fromJson(Map<String, dynamic> json) => HearitDetail(
        id: json['id'] is int ? json['id'] as int : int.parse(json['id'].toString()),
        title: json['title'] as String,
        summary: json['summary'] as String? ?? '',
        sources: (json['sources'] as List<dynamic>? ?? [])
            .map((e) => HearitSource.fromJson(e as Map<String, dynamic>))
            .toList(),
        playTime: Duration(seconds: json['playTime'] as int? ?? 0),
        lastPlayTime: json['lastPlayTime'] == null
            ? null
            : Duration(seconds: json['lastPlayTime'] as int),
        createdAt: DateTime.parse(json['createdAt'] as String),
        isBookmarked: json['isBookmarked'] as bool? ?? false,
        bookmarkId: json['bookmarkId'] as int?,
        category: HearitCategory.fromJson(json['category'] as Map<String, dynamic>),
        keywords: (json['keywords'] as List<dynamic>? ?? [])
            .map((e) => HearitKeyword.fromJson(e as Map<String, dynamic>))
            .toList(),
      );

  factory HearitDetail.fromSummaryStub({
    required int id,
    required String title,
    required String categoryName,
    required Color accentColor,
    required DateTime createdAt,
    Duration? lastPlayTime,
  }) =>
      HearitDetail(
        id: id,
        title: title,
        summary: '',
        sources: const [],
        playTime: Duration.zero,
        lastPlayTime: lastPlayTime,
        createdAt: createdAt,
        isBookmarked: false,
        bookmarkId: null,
        category: HearitCategory(
          id: 0,
          name: categoryName,
          color: accentColor,
        ),
        keywords: const [],
      );
}

Color _parseColor(String code) {
  final cleaned = code.replaceAll('#', '');
  final value = int.tryParse(cleaned, radix: 16);
  if (value == null) {
    return AppColors.hearitPurple1;
  }
  return Color(0xFF000000 | value);
}
