import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:kakao_flutter_sdk_user/kakao_flutter_sdk_user.dart';

import '../../core/network/api_client.dart';
import '../../core/network/api_exception.dart';
import 'models/auth_tokens.dart';
import 'models/kakao_user.dart';

class AuthRepository {
  AuthRepository({ApiClient? apiClient})
    : _apiClient = apiClient ?? ApiClient();

  final ApiClient _apiClient;

  /// 카카오 로그인 (앱 -> 웹 폴백 자동)
  Future<String> loginWithKakao() async {
    OAuthToken token;

    // 카카오톡 실행 가능 여부 확인
    // 카카오톡 실행이 가능하면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
    if (await isKakaoTalkInstalled()) {
      try {
        token = await UserApi.instance.loginWithKakaoTalk();
        debugPrint('카카오톡으로 로그인 성공');
      } catch (error) {
        debugPrint('카카오톡으로 로그인 실패 $error');

        // 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우,
        // 의도적인 로그인 취소로 보고 카카오계정으로 로그인 시도 없이 로그인 취소로 처리 (예: 뒤로 가기)
        if (error is PlatformException && error.code == 'CANCELED') {
          throw Exception('카카오 로그인이 취소되었습니다.');
        }

        // 카카오톡에 연결된 카카오계정이 없는 경우, 카카오계정으로 로그인
        try {
          token = await UserApi.instance.loginWithKakaoAccount();
          debugPrint('카카오계정으로 로그인 성공');
        } catch (error) {
          debugPrint('카카오계정으로 로그인 실패 $error');
          throw Exception('카카오 로그인 실패: $error');
        }
      }
    } else {
      try {
        token = await UserApi.instance.loginWithKakaoAccount();
        debugPrint('카카오계정으로 로그인 성공');
      } catch (error) {
        debugPrint('카카오계정으로 로그인 실패 $error');
        throw Exception('카카오 로그인 실패: $error');
      }
    }

    return token.accessToken;
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
  /// - 200 OK → true
  /// - 401 → false (토큰 만료, refresh 시도 대상)
  /// - 네트워크 오류 / 타임아웃 / 5xx → rethrow (refresh 시도 없이 처리)
  Future<bool> checkAuthStatus(String accessToken) async {
    try {
      await _apiClient.get<void>(
        '/api/v1/auth/check',
        headers: {'Authorization': 'Bearer $accessToken'},
      );
      return true;
    } on ApiException catch (e) {
      if (e.statusCode == 401) {
        return false;
      }
      rethrow;
    }
  }

  /// refreshToken으로 새 accessToken 발급
  Future<String> refreshAccessToken(String refreshToken) async {
    final response = await _apiClient.post<Map<String, dynamic>>(
      '/api/v1/auth/token/refresh',
      body: {'refreshToken': refreshToken},
      parser: (data) => data as Map<String, dynamic>,
    );
    final accessToken = response['accessToken'];
    if (accessToken is! String || accessToken.isEmpty) {
      throw ApiException.unexpected('응답에 유효한 accessToken이 없습니다.');
    }
    return accessToken;
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
