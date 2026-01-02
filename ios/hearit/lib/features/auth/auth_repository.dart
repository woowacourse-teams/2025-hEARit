import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:kakao_flutter_sdk_user/kakao_flutter_sdk_user.dart';

import '../../core/network/api_client.dart';
import 'models/auth_tokens.dart';
import 'models/kakao_user.dart';

class AuthRepository {
  AuthRepository({ApiClient? apiClient})
    : _apiClient = apiClient ?? ApiClient();

  final ApiClient _apiClient;

  /// 카카오 로그인 (앱 -> 웹 폴백 자동)
  Future<String> loginWithKakao() async {
    try {
      OAuthToken token;

      // 카카오톡 설치 여부 확인 후 자동 선택
      if (await isKakaoTalkInstalled()) {
        token = await UserApi.instance.loginWithKakaoTalk();
      } else {
        token = await UserApi.instance.loginWithKakaoAccount();
      }

      return token.accessToken;
    } catch (error) {
      // 사용자가 취소한 경우
      if (error is PlatformException && error.code == 'CANCELED') {
        throw Exception('카카오 로그인이 취소되었습니다.');
      }
      throw Exception('카카오 로그인 실패: $error');
    }
  }

  /// 카카오 사용자 정보 가져오기 (추후 확장용)
  Future<KakaoUser> getKakaoUserInfo() async {
    try {
      final user = await UserApi.instance.me();
      return KakaoUser.fromKakaoAccount(user);
    } catch (error) {
      throw Exception('사용자 정보 조회 실패: $error');
    }
  }

  /// 백엔드에 카카오 accessToken 전달 -> 자체 토큰 발급
  Future<AuthTokens> loginToBackend(String kakaoAccessToken) async {
    try {
      final response = await _apiClient.post<Map<String, dynamic>>(
        '/api/v1/auth/kakao-login',
        body: {'accessToken': kakaoAccessToken},
        parser: (data) => data as Map<String, dynamic>,
      );

      return AuthTokens.fromJson(response);
    } catch (error) {
      throw Exception('백엔드 로그인 실패: $error');
    }
  }

  /// 백엔드 인증 상태 체크 (/api/v1/auth/check)
  Future<bool> checkAuthStatus(String accessToken) async {
    try {
      await _apiClient.get<void>(
        '/api/v1/auth/check',
        headers: {'Authorization': 'Bearer $accessToken'},
      );
      return true; // 200 OK
    } catch (error) {
      debugPrint('인증 체크 실패: $error');
      return false; // 401 또는 기타 에러
    }
  }

  /// 카카오 로그아웃
  Future<void> logoutFromKakao() async {
    try {
      await UserApi.instance.logout();
    } catch (error) {
      debugPrint('카카오 로그아웃 실패 (무시): $error');
    }
  }
}
