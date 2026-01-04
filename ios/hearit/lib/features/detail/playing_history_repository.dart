import 'package:flutter/foundation.dart';

import '../../core/network/api_client.dart';
import '../auth/services/auth_storage_service.dart';

/// 재생 기록 저장 Repository
class PlayingHistoryRepository {
  PlayingHistoryRepository({
    ApiClient? apiClient,
    AuthStorageService? authStorageService,
  }) : _apiClient = apiClient ?? ApiClient(),
       _authStorageService = authStorageService ?? AuthStorageService();

  final ApiClient _apiClient;
  final AuthStorageService _authStorageService;

  /// 재생 기록 저장
  /// POST /api/v1/playing-histories
  ///
  /// [hearitId]: 팟캐스트 ID
  /// [lastPlayTime]: 마지막 재생 위치 (밀리초)
  /// [clientEventTime]: 클라이언트 이벤트 발생 시간 (Unix Epoch Time, 밀리초)
  Future<void> savePlayingHistory({
    required int hearitId,
    required int lastPlayTime,
    required int clientEventTime,
  }) async {
    try {
      // 1. 토큰 가져오기 (로그인 사용자만)
      final token = await _authStorageService.getAccessToken();

      // 2. 헤더 구성 (토큰이 있으면 Authorization 헤더 추가)
      final headers = (token != null && token.isNotEmpty)
          ? {'Authorization': 'Bearer $token'}
          : null;

      // 3. 요청 바디 구성
      final body = {
        'hearitId': hearitId,
        'lastPlayTime': lastPlayTime,
        'clientEventTime': clientEventTime,
      };

      debugPrint(
        '📝 재생 기록 저장 API 호출: hearitId=$hearitId, '
        'lastPlayTime=${lastPlayTime}ms, clientEventTime=$clientEventTime',
      );

      // 4. API 호출 (UUID는 DeviceUUIDInterceptor가 자동 추가)
      await _apiClient.post<void>(
        '/api/v1/playing-histories',
        headers: headers,
        body: body,
      );

      debugPrint('✅ 재생 기록 저장 성공: hearitId=$hearitId');
    } catch (error) {
      debugPrint('❌ 재생 기록 저장 실패: $error');
      rethrow; // Service 레이어에서 로컬 큐 처리
    }
  }
}
