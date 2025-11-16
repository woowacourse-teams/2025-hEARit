import 'package:flutter/material.dart';

class RecommendCardData {
  const RecommendCardData({
    required this.id,
    required this.categoryName,
    required this.title,
    required this.categoryColor,
    required this.createdAt,
  });

  final int id;
  final String categoryName;
  final String title;
  final Color categoryColor;
  final DateTime createdAt;
}

class ListeningCardData {
  const ListeningCardData({
    required this.id,
    required this.title,
    required this.description,
    required this.backgroundColor,
    required this.createdAt,
    this.progress,
  });

  final int id;
  final String title;
  final String description;
  final Color backgroundColor;
  final DateTime createdAt;
  final double? progress;
}

class CategoryPodcastData {
  const CategoryPodcastData({
    required this.id,
    required this.title,
    required this.createdAt,
  });

  final int id;
  final String title;
  final DateTime createdAt;
}

class CategorySectionData {
  const CategorySectionData({
    required this.categoryName,
    required this.accentColor,
    required this.podcasts,
  });

  final String categoryName;
  final Color accentColor;
  final List<CategoryPodcastData> podcasts;
}
