import 'package:flutter/material.dart';

import '../../core/network/api_client.dart';
import '../auth/services/auth_storage_service.dart';
import 'search_models.dart';

class SearchRepository {
  SearchRepository({
    ApiClient? apiClient,
    AuthStorageService? authStorageService,
  }) : _apiClient = apiClient ?? ApiClient(),
       _authStorageService = authStorageService ?? AuthStorageService();

  final ApiClient _apiClient;
  final AuthStorageService _authStorageService;

  Future<List<SearchHearit>> searchHearits(
    String term, {
    int page = 0,
    int size = 20,
  }) async {
    // 토큰 가져오기 (로그인 사용자만)
    final token = await _authStorageService.getAccessToken();
    final headers = (token != null && token.isNotEmpty)
        ? {'Authorization': 'Bearer $token'}
        : null;

    final Map<String, dynamic> data = await _apiClient
        .get<Map<String, dynamic>>(
          '/api/v1/hearits/search',
          queryParameters: {'searchTerm': term, 'page': page, 'size': size},
          headers: headers,
          parser: (raw) => raw as Map<String, dynamic>? ?? <String, dynamic>{},
        );
    final List<dynamic> content = data['content'] as List<dynamic>? ?? const [];
    return content.whereType<Map<String, dynamic>>().map(_mapHearit).toList();
  }

  Future<List<SearchCategory>> fetchCategories({
    int page = 0,
    int size = 20,
  }) async {
    final Map<String, dynamic> data = await _apiClient
        .get<Map<String, dynamic>>(
          '/api/v1/categories',
          queryParameters: {'page': page, 'size': size},
          parser: (raw) => raw as Map<String, dynamic>? ?? <String, dynamic>{},
        );
    final List<dynamic> content = data['content'] as List<dynamic>? ?? const [];
    return content.whereType<Map<String, dynamic>>().map(_mapCategory).toList();
  }

  SearchCategory _mapCategory(Map<String, dynamic> json) {
    return SearchCategory(
      id: _asInt(json['id']),
      name: json['name'] as String? ?? '',
      color: _parseColor(json['colorCode'] as String?),
    );
  }

  Future<List<SearchHearit>> fetchHearitsByCategory({
    required int categoryId,
    int page = 0,
    int size = 20,
  }) async {
    // 토큰 가져오기 (로그인 사용자만)
    final token = await _authStorageService.getAccessToken();
    final headers = (token != null && token.isNotEmpty)
        ? {'Authorization': 'Bearer $token'}
        : null;

    final Map<String, dynamic> data = await _apiClient
        .get<Map<String, dynamic>>(
          '/api/v1/hearits',
          queryParameters: {
            'categoryId': categoryId,
            'sort': 'createdAt,desc',
            'page': page,
            'size': size,
          },
          headers: headers,
          parser: (raw) => raw as Map<String, dynamic>? ?? <String, dynamic>{},
        );
    final List<dynamic> content = data['content'] as List<dynamic>? ?? const [];
    return content.whereType<Map<String, dynamic>>().map(_mapHearit).toList();
  }

  SearchHearit _mapHearit(Map<String, dynamic> json) {
    final keywords = (json['keywords'] as List<dynamic>? ?? const [])
        .whereType<Map<String, dynamic>>()
        .map((e) => e['name'] as String? ?? '')
        .where((name) => name.isNotEmpty)
        .toList();

    return SearchHearit(
      id: _asInt(json['id']),
      title: json['title'] as String? ?? '',
      playTimeSeconds: (json['playTime'] as num?)?.toInt() ?? 0,
      lastPlayTimeSeconds: (json['lastPlayTime'] as num?) != null
          ? (json['lastPlayTime'] as num).toInt()
          : null,
      isFinished: json['isFinished'] as bool?,
      keywords: keywords,
    );
  }

  int _asInt(dynamic value) {
    if (value is int) return value;
    return int.tryParse(value.toString()) ?? 0;
  }

  Color _parseColor(String? hex) {
    if (hex == null || hex.isEmpty) return const Color(0xFF3B3B46);
    final cleaned = hex.replaceAll('#', '');
    if (cleaned.length == 6) {
      return Color(int.parse('FF$cleaned', radix: 16));
    }
    if (cleaned.length == 8) {
      return Color(int.parse(cleaned, radix: 16));
    }
    return const Color(0xFF3B3B46);
  }
}
