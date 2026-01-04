import 'package:flutter/material.dart';

import '../../core/network/api_client.dart';
import '../../core/network/api_exception.dart';
import '../auth/services/auth_storage_service.dart';
import 'home_models.dart';

enum AuthCheckStatus {
  ok, // 200 OK
  unauthorized, // 401 Unauthorized
  error, // 네트워크 에러 등
}

class HomeRepository {
  HomeRepository({ApiClient? apiClient})
    : _apiClient = apiClient ?? ApiClient();

  final ApiClient _apiClient;

  /// 토큰 존재 여부 확인
  Future<bool> hasValidToken() async {
    final token = await AuthStorageService().getAccessToken();
    return token != null && token.isNotEmpty;
  }

  /// 인증 상태 체크 (/api/v1/auth/check)
  Future<AuthCheckStatus> checkAuth() async {
    try {
      final token = await AuthStorageService().getAccessToken();
      if (token == null || token.isEmpty) {
        return AuthCheckStatus.error;
      }

      await _apiClient.get<void>(
        '/api/v1/auth/check',
        headers: {'Authorization': 'Bearer $token'},
      );
      return AuthCheckStatus.ok; // 200 OK
    } on ApiException catch (e) {
      if (e.statusCode == 401) {
        return AuthCheckStatus.unauthorized; // 401
      }
      debugPrint('Auth check failed: $e');
      return AuthCheckStatus.error; // 기타 에러
    } catch (error) {
      debugPrint('Auth check unexpected error: $error');
      return AuthCheckStatus.error;
    }
  }

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
    // 로그인 사용자는 토큰 기준, 게스트는 UUID 기준으로 재생 기록 조회
    final token = await AuthStorageService().getAccessToken();
    final headers = (token != null && token.isNotEmpty)
        ? {'Authorization': 'Bearer $token'}
        : null;

    final List<dynamic> data = await _apiClient.get<List<dynamic>>(
      '/api/v1/playing-histories/hearits',
      headers: headers,
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
    final token = await AuthStorageService().getAccessToken();
    if (token == null || token.isEmpty) {
      return const []; // 토큰 없으면 빈 리스트
    }

    final List<dynamic> data = await _apiClient.get<List<dynamic>>(
      '/api/v1/bookmarks',
      headers: {'Authorization': 'Bearer $token'},
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

  /// 사용자 닉네임 조회 (로그인 사용자만)
  Future<String?> fetchUserNickname() async {
    try {
      final token = await AuthStorageService().getAccessToken();
      if (token == null || token.isEmpty) {
        return null; // 게스트 모드
      }

      final data = await _apiClient.get<Map<String, dynamic>>(
        '/api/v1/members/me',
        headers: {'Authorization': 'Bearer $token'},
        parser: (raw) => raw as Map<String, dynamic>,
      );

      return data['nickname'] as String?;
    } catch (e) {
      debugPrint('Failed to fetch user nickname: $e');
      return null; // 실패 시 null 반환 (기본값 사용)
    }
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
    final playTime = (json['playTime'] as num?)?.toInt() ?? 0; // 초 단위
    final lastPlayTimeMs = (json['lastPlayTime'] as num?)?.toInt(); // 밀리초 단위
    final progress = includeProgress && playTime > 0 && lastPlayTimeMs != null
        ? ((lastPlayTimeMs / 1000) / playTime).clamp(0, 1).toDouble()
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
      categoryId: _asInt(json['categoryId'] ?? json['id']),
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
