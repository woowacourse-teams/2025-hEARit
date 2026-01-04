import 'dart:convert';

import 'package:dio/dio.dart';

import '../../core/network/api_client.dart';
import '../auth/services/auth_storage_service.dart';
import 'hearit_detail.dart';
import 'hearit_detail_viewmodel.dart';

class DetailRepository {
  DetailRepository({
    ApiClient? apiClient,
    AuthStorageService? authStorageService,
  }) : _apiClient = apiClient ?? ApiClient(),
       _authStorageService = authStorageService ?? AuthStorageService(),
       _dio = Dio(
         BaseOptions(
           connectTimeout: const Duration(seconds: 5),
           receiveTimeout: const Duration(seconds: 10),
           sendTimeout: const Duration(seconds: 10),
         ),
       );

  final ApiClient _apiClient;
  final AuthStorageService _authStorageService;
  final Dio _dio;

  Future<HearitDetail> fetchDetail(int hearitId) async {
    // 로그인한 사용자인지 확인
    final token = await _authStorageService.getAccessToken();
    final headers = token != null ? {'Authorization': 'Bearer $token'} : null;

    final Map<String, dynamic> data = await _apiClient
        .get<Map<String, dynamic>>(
          '/api/v1/hearits/$hearitId',
          headers: headers,
          parser: (raw) => raw as Map<String, dynamic>,
        );
    return HearitDetail.fromJson(data);
  }

  Future<String?> fetchOriginalAudioUrl(int hearitId) async {
    final Map<String, dynamic> data = await _apiClient
        .get<Map<String, dynamic>>(
          '/api/v1/hearits/$hearitId/original-audio-url',
          parser: (raw) => raw as Map<String, dynamic>,
        );
    return data['url'] as String?;
  }

  Future<String?> fetchScriptUrl(int hearitId) async {
    final Map<String, dynamic> data = await _apiClient
        .get<Map<String, dynamic>>(
          '/api/v1/hearits/$hearitId/script-url',
          parser: (raw) => raw as Map<String, dynamic>,
        );
    return data['url'] as String?;
  }

  Future<List<ScriptLine>> fetchScripts(int hearitId) async {
    final scriptUrl = await fetchScriptUrl(hearitId);
    if (scriptUrl == null || scriptUrl.isEmpty) return [];

    final response = await _dio.get<dynamic>(scriptUrl);
    dynamic raw = response.data;
    if (raw is String) {
      try {
        raw = jsonDecode(raw);
      } catch (_) {
        return [];
      }
    }
    if (raw is! List) return [];
    return raw
        .whereType<Map<String, dynamic>>()
        .map(ScriptLine.fromJson)
        .toList();
  }

  /// 북마크 생성
  /// POST /api/v1/bookmarks/hearits/{hearitId}
  Future<int> createBookmark(int hearitId, String token) async {
    final response = await _apiClient.post<Map<String, dynamic>>(
      '/api/v1/bookmarks/hearits/$hearitId',
      headers: {'Authorization': 'Bearer $token'},
      parser: (data) => data as Map<String, dynamic>,
    );
    return response['id'] as int;
  }

  /// 북마크 삭제
  /// DELETE /api/v1/bookmarks/{bookmarkId}
  Future<void> deleteBookmark(int bookmarkId, String token) async {
    await _apiClient.delete<void>(
      '/api/v1/bookmarks/$bookmarkId',
      headers: {'Authorization': 'Bearer $token'},
    );
  }
}
