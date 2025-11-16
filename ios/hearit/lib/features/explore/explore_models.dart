import 'package:flutter/material.dart';

import '../detail/hearit_detail_viewmodel.dart';

class ExploreFeedItem {
  ExploreFeedItem({
    required this.id,
    required this.title,
    required this.categoryColorCode,
    required this.keywords,
    required this.cursorId,
    required this.isBookmarked,
    required this.bookmarkId,
  });

  final int id;
  final String title;
  final String categoryColorCode;
  final List<String> keywords;
  final int cursorId;
  final bool isBookmarked;
  final int? bookmarkId;

  String? shortAudioUrl;
  String? scriptFileUrl;
  List<ScriptLine>? scripts;
  bool detailLoaded = false;
  bool detailLoading = false;

  Color get categoryColor => Color(
        int.parse(categoryColorCode.replaceFirst('#', '0xFF')),
      );

  factory ExploreFeedItem.fromJson(Map<String, dynamic> json) {
    return ExploreFeedItem(
      id: json['id'] as int,
      title: json['title'] as String,
      categoryColorCode: json['categoryColorCode'] as String,
      keywords: (json['keywords'] as List<dynamic>? ?? [])
          .whereType<Map<String, dynamic>>()
          .map((e) => e['name'] as String)
          .toList(),
      cursorId: json['cursorId'] as int? ?? 0,
      isBookmarked: json['isBookmarked'] as bool? ?? false,
      bookmarkId: json['bookmarkId'] as int?,
    );
  }
}

class ExplorePage {
  const ExplorePage({
    required this.items,
    required this.isEmpty,
  });

  final List<ExploreFeedItem> items;
  final bool isEmpty;

  factory ExplorePage.fromJson(Map<String, dynamic> json) {
    final content = (json['content'] as List<dynamic>? ?? [])
        .whereType<Map<String, dynamic>>()
        .map(ExploreFeedItem.fromJson)
        .toList();
    return ExplorePage(
      items: content,
      isEmpty: json['isEmpty'] as bool? ?? content.isEmpty,
    );
  }
}

class ExploreAssetUrls {
  const ExploreAssetUrls({this.scriptUrl, this.shortAudioUrl});

  final String? scriptUrl;
  final String? shortAudioUrl;
}
