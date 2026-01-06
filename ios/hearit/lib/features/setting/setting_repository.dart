import 'package:flutter/foundation.dart';

import '../../core/network/api_client.dart';
import '../auth/services/auth_storage_service.dart';
import 'models/member_profile.dart';

class SettingRepository {
  SettingRepository({
    ApiClient? apiClient,
    AuthStorageService? authStorageService,
  }) : _apiClient = apiClient ?? ApiClient(),
       _authStorageService = authStorageService ?? AuthStorageService();

  final ApiClient _apiClient;
  final AuthStorageService _authStorageService;

  /// 내 프로필 정보 가져오기
  /// GET /api/v1/members/me
  Future<MemberProfile> fetchMyProfile() async {
    try {
      // 1. 저장된 토큰 가져오기
      final token = await _authStorageService.getAccessToken();

      if (token == null) {
        throw Exception('로그인이 필요합니다.');
      }

      // 2. API 호출
      final response = await _apiClient.get<Map<String, dynamic>>(
        '/api/v1/members/me',
        headers: {'Authorization': 'Bearer $token'},
        parser: (data) => data as Map<String, dynamic>,
      );

      // 3. MemberProfile 객체로 변환
      return MemberProfile.fromJson(response);
    } catch (error) {
      debugPrint('프로필 조회 실패: $error');
      throw Exception('프로필 정보를 불러올 수 없습니다: $error');
    }
  }

  /// 회원탈퇴
  /// DELETE /api/v1/auth/withdraw
  /// Headers: Authorization (Bearer token), UUID (자동 추가됨)
  Future<void> withdrawAccount() async {
    try {
      final token = await _authStorageService.getAccessToken();

      if (token == null) {
        throw Exception('로그인이 필요합니다.');
      }

      await _apiClient.delete<void>(
        '/api/v1/auth/withdraw',
        headers: {'Authorization': 'Bearer $token'},
      );
    } catch (error) {
      debugPrint('회원탈퇴 API 실패: $error');
      throw Exception('회원탈퇴에 실패했습니다.');
    }
  }
}
