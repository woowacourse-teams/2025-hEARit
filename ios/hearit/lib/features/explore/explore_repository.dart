import 'dart:convert';

import 'package:dio/dio.dart';

import '../../core/network/api_client.dart';
import '../detail/hearit_detail_viewmodel.dart';
import 'explore_models.dart';

class ExploreRepository {
  ExploreRepository({
    ApiClient? apiClient,
    Dio? dio,
  }) : _apiClient = apiClient ?? ApiClient(),
       _dio = dio ??
           Dio(
             BaseOptions(
               connectTimeout: const Duration(seconds: 5),
               receiveTimeout: const Duration(seconds: 10),
               sendTimeout: const Duration(seconds: 10),
             ),
           );

  final ApiClient _apiClient;
  final Dio _dio;

  Future<ExplorePage> fetchExplorePage({
    required int cursorId,
    int size = 10,
  }) async {
    final data = await _apiClient.get<Map<String, dynamic>>(
      '/api/v2/hearits/explore',
      queryParameters: {'cursorId': cursorId, 'size': size},
      parser: (raw) => raw as Map<String, dynamic>,
    );
    return ExplorePage.fromJson(data);
  }

  Future<ExploreAssetUrls> fetchShortAssets(int hearitId) async {
    final script = await _apiClient.get<Map<String, dynamic>>(
      '/api/v1/hearits/$hearitId/script-url',
      parser: (raw) => raw as Map<String, dynamic>,
    );
    final audio = await _apiClient.get<Map<String, dynamic>>(
      '/api/v1/hearits/$hearitId/short-audio-url',
      parser: (raw) => raw as Map<String, dynamic>,
    );
    return ExploreAssetUrls(
      scriptUrl: script['url'] as String?,
      shortAudioUrl: audio['url'] as String?,
    );
  }

  Future<List<ScriptLine>> fetchScriptsFromUrl(String url) async {
    final response = await _dio.get<dynamic>(url);
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
}
