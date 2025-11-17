import 'package:flutter/material.dart';

import '../../core/network/api_client.dart';
import 'home_models.dart';

class HomeRepository {
  HomeRepository({ApiClient? apiClient})
    : _apiClient = apiClient ?? ApiClient();

  final ApiClient _apiClient;

  Future<List<RecommendCardData>> fetchRecommendations() async {
    final List<dynamic> data = await _apiClient.get<List<dynamic>>(
      '/api/v1/hearits/recommend',
      parser: _asList,
    );
    return data
        .whereType<Map<String, dynamic>>()
        .map(_mapRecommendation)
        .toList();
  }

  Future<List<ListeningCardData>> fetchListeningNow() async {
    final List<dynamic> data = await _apiClient.get<List<dynamic>>(
      '/api/v1/playing-histories/hearits',
      parser: _asList,
    );
    return data
        .whereType<Map<String, dynamic>>()
        .map((json) => _mapListening(json, includeProgress: true))
        .toList();
  }

  Future<List<ListeningCardData>> fetchRecentlyAdded() async {
    final List<dynamic> data = await _apiClient.get<List<dynamic>>(
      '/api/v1/hearits',
      queryParameters: {'sort': 'createdAt'},
      parser: (raw) => _asList(raw, contentKey: 'content'),
    );
    return data
        .whereType<Map<String, dynamic>>()
        .map((json) => _mapListening(json, includeProgress: false))
        .toList();
  }

  Future<List<ListeningCardData>> fetchBookmarked() async {
    final List<dynamic> data = await _apiClient.get<List<dynamic>>(
      '/api/v1/bookmarks',
      queryParameters: {
        'page': 0,
        'size': 10,
        'filter': 'unfinished',
        'sort': 'createdAt,desc',
      },
      parser: (raw) => _asList(raw, contentKey: 'content'),
    );
    return data
        .whereType<Map<String, dynamic>>()
        .map((json) => _mapListening(json, includeProgress: true))
        .toList();
  }

  Future<List<CategorySectionData>> fetchCategoryRecommendations() async {
    final List<dynamic> data = await _apiClient.get<List<dynamic>>(
      '/api/v1/recommendations/categories',
      parser: _asList,
    );
    return data
        .whereType<Map<String, dynamic>>()
        .map(_mapCategorySection)
        .toList();
  }

  RecommendCardData _mapRecommendation(Map<String, dynamic> json) {
    return RecommendCardData(
      id: _asInt(json['id'] ?? json['hearitId']),
      categoryName: json['categoryName'] as String? ?? '추천',
      title: json['title'] as String? ?? '',
      categoryColor: _parseColor(
        json['categoryColor'] as String?,
        Colors.grey.shade800,
      ),
      createdAt: _parseDate(json['createdAt'] as String?),
    );
  }

  ListeningCardData _mapListening(
    Map<String, dynamic> json, {
    required bool includeProgress,
  }) {
    final category = json['category'] as Map<String, dynamic>?;
    final categoryName = category?['name'] as String? ?? 'Podcast';
    final colorCode = category?['colorCode'] as String?;
    final playTime = (json['playTime'] as num?)?.toInt() ?? 0;
    final lastPlayTime = (json['lastPlayTime'] as num?)?.toInt();
    final progress = includeProgress && playTime > 0 && lastPlayTime != null
        ? (lastPlayTime / playTime).clamp(0, 1).toDouble()
        : null;

    return ListeningCardData(
      id: _asInt(json['id']),
      title: categoryName,
      description: json['title'] as String? ?? '',
      backgroundColor: _parseColor(colorCode, Colors.blueGrey.shade700),
      createdAt: _parseDate(json['createdAt'] as String?),
      progress: progress,
    );
  }

  CategorySectionData _mapCategorySection(Map<String, dynamic> json) {
    final hearits = json['hearits'] as List<dynamic>? ?? const [];
    final accentColor = _parseColor(
      json['colorCode'] as String?,
      Colors.deepPurple.shade400,
    );

    final podcasts = hearits
        .whereType<Map<String, dynamic>>()
        .map(
          (item) => CategoryPodcastData(
            id: _asInt(item['id'] ?? item['hearitId']),
            title: item['title'] as String? ?? '',
            createdAt: _parseDate(item['createdAt'] as String?),
          ),
        )
        .toList();

    return CategorySectionData(
      categoryName: json['categoryName'] as String? ?? '카테고리',
      accentColor: accentColor,
      podcasts: podcasts,
    );
  }

  List<dynamic> _asList(dynamic raw, {String? contentKey}) {
    if (raw is List) return raw;
    if (raw is Map && contentKey != null && raw[contentKey] is List) {
      return raw[contentKey] as List<dynamic>;
    }
    return const [];
  }

  int _asInt(dynamic value) {
    if (value is int) return value;
    return int.tryParse(value.toString()) ?? 0;
  }

  DateTime _parseDate(String? isoString) {
    if (isoString == null) return DateTime.now();
    return DateTime.tryParse(isoString) ?? DateTime.now();
  }

  Color _parseColor(String? hex, Color fallback) {
    if (hex == null || hex.isEmpty) return fallback;
    final cleaned = hex.replaceAll('#', '');

    // Handle 6-digit RGB -> force opaque alpha, 8-digit ARGB as-is.
    if (cleaned.length == 6) {
      return Color(int.parse('FF$cleaned', radix: 16));
    }
    if (cleaned.length == 8) {
      return Color(int.parse(cleaned, radix: 16));
    }
    return fallback;
  }
}
