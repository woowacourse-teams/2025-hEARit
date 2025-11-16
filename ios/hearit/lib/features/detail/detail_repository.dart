import 'dart:convert';

import 'package:dio/dio.dart';

import '../../core/network/api_client.dart';
import 'hearit_detail.dart';
import 'hearit_detail_viewmodel.dart';

class DetailRepository {
  DetailRepository({ApiClient? apiClient})
    : _apiClient = apiClient ?? ApiClient(),
      _dio = Dio(
        BaseOptions(
          connectTimeout: const Duration(seconds: 5),
          receiveTimeout: const Duration(seconds: 10),
          sendTimeout: const Duration(seconds: 10),
        ),
      );

  final ApiClient _apiClient;
  final Dio _dio;

  Future<HearitDetail> fetchDetail(int hearitId) async {
    final Map<String, dynamic> data = await _apiClient
        .get<Map<String, dynamic>>(
          '/api/v1/hearits/$hearitId',
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
}
