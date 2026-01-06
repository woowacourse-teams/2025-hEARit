import 'package:flutter/foundation.dart';

import '../../core/network/api_client.dart';
import '../../core/network/api_exception.dart';
import '../auth/services/auth_storage_service.dart';
import 'library_models.dart';

/// 라이브러리(북마크) 관련 API 호출을 담당하는 Repository
class LibraryRepository {
  LibraryRepository({
    ApiClient? apiClient,
    AuthStorageService? authStorageService,
  }) : _apiClient = apiClient ?? ApiClient(),
       _authStorageService = authStorageService ?? AuthStorageService();

  final ApiClient _apiClient;
  final AuthStorageService _authStorageService;

  /// 북마크한 히어릿 목록 조회 (페이징)
  /// GET /api/v2/bookmarks/hearits?page={page}&size={size}
  Future<BookmarkListResponse> fetchBookmarks({
    required int page,
    required int size,
  }) async {
    try {
      // 저장된 토큰 가져오기
      final token = await _authStorageService.getAccessToken();

      if (token == null) {
        throw ApiException.server(401, '로그인이 필요합니다.');
      }

      // API 호출
      final response = await _apiClient.get<Map<String, dynamic>>(
        '/api/v2/bookmarks/hearits',
        queryParameters: {'page': page.toString(), 'size': size.toString()},
        headers: {'Authorization': 'Bearer $token'},
        parser: (data) => data as Map<String, dynamic>,
      );

      // BookmarkListResponse 객체로 변환
      return BookmarkListResponse.fromJson(response);
    } on ApiException {
      rethrow; // ApiException은 그대로 전달 (statusCode 보존)
    } catch (error) {
      debugPrint('북마크 목록 조회 실패: $error');
      throw Exception('북마크 목록을 불러올 수 없습니다: $error');
    }
  }

  /// 북마크 삭제
  /// DELETE /api/v1/bookmarks/{bookmarkId}
  Future<void> deleteBookmark(int bookmarkId) async {
    try {
      // 저장된 토큰 가져오기
      final token = await _authStorageService.getAccessToken();

      if (token == null) {
        throw ApiException.server(401, '로그인이 필요합니다.');
      }

      // API 호출
      await _apiClient.delete<void>(
        '/api/v1/bookmarks/$bookmarkId',
        headers: {'Authorization': 'Bearer $token'},
      );

      debugPrint('북마크 삭제 성공: $bookmarkId');
    } on ApiException {
      rethrow; // ApiException은 그대로 전달 (statusCode 보존)
    } catch (error) {
      debugPrint('북마크 삭제 실패: $error');
      throw Exception('북마크를 삭제할 수 없습니다: $error');
    }
  }

  /// 히어릿 오디오 원본 URL 조회
  /// GET /api/v1/hearits/{hearitId}/original-audio-url
  Future<String?> fetchOriginalAudioUrl(int hearitId) async {
    try {
      final response = await _apiClient.get<Map<String, dynamic>>(
        '/api/v1/hearits/$hearitId/original-audio-url',
        parser: (data) => data as Map<String, dynamic>,
      );

      return response['url'] as String?;
    } catch (error) {
      debugPrint('오디오 URL 조회 실패 (hearitId: $hearitId): $error');
      return null;
    }
  }
}
