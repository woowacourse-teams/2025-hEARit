import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import '../models/auth_tokens.dart';

class AuthStorageService {
  static const _accessTokenKey = 'access_token';
  static const _refreshTokenKey = 'refresh_token';
  static const _isGuestModeKey = 'is_guest_mode';
  static const _storage = FlutterSecureStorage();

  /// 토큰 저장 (로그인 성공 시)
  Future<void> saveTokens(AuthTokens tokens) async {
    await Future.wait([
      _storage.write(key: _accessTokenKey, value: tokens.accessToken),
      _storage.write(key: _refreshTokenKey, value: tokens.refreshToken),
      _storage.delete(key: _isGuestModeKey), // 로그인 시 게스트 모드 해제
    ]);
  }

  /// 토큰 조회 (앱 시작 시 자동 로그인 체크)
  Future<AuthTokens?> getTokens() async {
    final results = await Future.wait([
      _storage.read(key: _accessTokenKey),
      _storage.read(key: _refreshTokenKey),
    ]);

    final accessToken = results[0];
    final refreshToken = results[1];

    if (accessToken == null || refreshToken == null) return null;

    return AuthTokens(accessToken: accessToken, refreshToken: refreshToken);
  }

  /// AccessToken만 조회 (API 호출 시)
  Future<String?> getAccessToken() async {
    return await _storage.read(key: _accessTokenKey);
  }

  /// 토큰 삭제 (로그아웃 시)
  Future<void> clearTokens() async {
    await Future.wait([
      _storage.delete(key: _accessTokenKey),
      _storage.delete(key: _refreshTokenKey),
    ]);
  }

  /// 게스트 모드 설정
  Future<void> setGuestMode(bool isGuest) async {
    if (isGuest) {
      await _storage.write(key: _isGuestModeKey, value: 'true');
    } else {
      await _storage.delete(key: _isGuestModeKey);
    }
  }

  /// 게스트 모드 확인
  Future<bool> isGuestMode() async {
    final value = await _storage.read(key: _isGuestModeKey);
    return value == 'true';
  }
}
