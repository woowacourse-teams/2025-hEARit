import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/widgets.dart';

import '../../core/network/api_client.dart';
import 'auth_repository.dart';
import 'models/auth_event.dart';
import 'models/auth_tokens.dart';
import 'models/kakao_user.dart';
import 'services/auth_storage_service.dart';

enum AuthStatus {
  initial, // 초기 상태 (인증 체크 전)
  checking, // 인증 체크 중 (스플래시 화면)
  authenticated, // 로그인됨
  unauthenticated, // 로그인 안됨 (로그인 화면 필요)
  guest, // 게스트 모드
}

class AuthViewModel extends ChangeNotifier {
  AuthViewModel({
    AuthRepository? repository,
    AuthStorageService? storageService,
    StreamController<AuthEvent>? authEventController,
  }) : _storageService = storageService ?? AuthStorageService() {
    _authEventController =
        authEventController ?? StreamController<AuthEvent>.broadcast();
    _repository = repository ??
        AuthRepository(
          apiClient: ApiClient(
            storageService: _storageService,
            authEventController: _authEventController,
          ),
        );
    _authEventSubscription = _authEventController.stream.listen((event) {
      if (event == AuthEvent.tokenRefreshFailed) {
        unawaited(clearAuthStatus());
      }
    });
  }

  late final AuthRepository _repository;
  final AuthStorageService _storageService;
  late final StreamController<AuthEvent> _authEventController;
  late final StreamSubscription<AuthEvent> _authEventSubscription;

  AuthStatus _status = AuthStatus.initial;
  bool _isLoading = false;
  KakaoUser? _user;
  String? _errorMessage;

  AuthStatus get status => _status;
  bool get isLoading => _isLoading;
  bool get isLoggedIn => _status == AuthStatus.authenticated;
  bool get isGuest => _status == AuthStatus.guest;
  KakaoUser? get user => _user;
  String? get errorMessage => _errorMessage;

  /// 앱 시작 시 인증 상태 체크
  Future<void> checkAuthStatus() async {
    _status = AuthStatus.checking;
    notifyListeners();

    try {
      // 1. 저장된 토큰 확인 (게스트 모드는 앱 재시작 시 초기화됨)
      final tokens = await _storageService.getTokens();
      if (tokens == null) {
        _status = AuthStatus.unauthenticated;
        notifyListeners();
        return;
      }

      // 2. 백엔드에 토큰 유효성 확인 (/api/v1/auth/check)
      final isValid = await _repository.checkAuthStatus(tokens.accessToken);
      if (isValid) {
        _status = AuthStatus.authenticated;
        // 사용자 정보 가져오기 (현재는 사용 안 함, 추후 확장용)
        try {
          _user = await _repository.getKakaoUserInfo();
        } catch (e) {
          debugPrint('사용자 정보 조회 실패: $e');
        }
      } else {
        // accessToken 만료 -> refreshToken으로 갱신 시도
        try {
          final newAccessToken =
              await _repository.refreshAccessToken(tokens.refreshToken);
          await _storageService.saveTokens(
            AuthTokens(
              accessToken: newAccessToken,
              refreshToken: tokens.refreshToken,
            ),
          );
          _status = AuthStatus.authenticated;
          try {
            _user = await _repository.getKakaoUserInfo();
          } catch (e) {
            debugPrint('사용자 정보 조회 실패: $e');
          }
        } catch (e) {
          // refreshToken도 만료 -> 로그아웃
          debugPrint('토큰 갱신 실패, 로그아웃 처리: $e');
          await _storageService.clearTokens();
          _status = AuthStatus.unauthenticated;
        }
      }
    } catch (error) {
      debugPrint('인증 체크 오류: $error');
      _status = AuthStatus.unauthenticated;
    }

    notifyListeners();
  }

  /// 카카오 로그인 수행
  Future<void> loginWithKakao() async {
    _setLoading(true);
    _errorMessage = null;

    try {
      // 1. 카카오 로그인 (앱 -> 웹 폴백 자동)
      final kakaoAccessToken = await _repository.loginWithKakao();

      // 2. 백엔드에 카카오 토큰 전달하여 자체 토큰 발급
      final authTokens = await _repository.loginToBackend(kakaoAccessToken);

      // 3. 토큰 저장 (SecureStorage)
      await _storageService.saveTokens(authTokens);

      // 4. 사용자 정보 가져오기 (현재는 사용 안 함, 추후 확장용)
      try {
        _user = await _repository.getKakaoUserInfo();
      } catch (e) {
        debugPrint('사용자 정보 조회 실패: $e');
      }

      _status = AuthStatus.authenticated;
      debugPrint('카카오 로그인 성공');
      _setLoading(false);
    } catch (error) {
      debugPrint('카카오 로그인 실패: $error');
      _errorMessage = error.toString();
      _status = AuthStatus.unauthenticated;
      _setLoading(false);
      rethrow;
    }
  }

  /// 게스트 모드로 진입 (세션 동안만 유지, 앱 재시작 시 초기화)
  Future<void> enterGuestMode() async {
    // 메모리 상태만 변경 (저장소에 저장하지 않음)
    _status = AuthStatus.guest;
    notifyListeners();
  }

  /// 로그아웃 (추후 설정 화면에서 사용)
  Future<void> logout() async {
    _setLoading(true);

    try {
      // 1. 카카오 로그아웃
      await _repository.logoutFromKakao();

      // 2. 로컬 토큰 삭제
      await _storageService.clearTokens();

      _status = AuthStatus.unauthenticated;
      _user = null;
      _setLoading(false);
    } catch (error) {
      _errorMessage = error.toString();
      _setLoading(false);
    }
  }

  /// 401 에러 발생 시 인증 상태 초기화
  Future<void> clearAuthStatus() async {
    await _storageService.clearTokens();
    _status = AuthStatus.unauthenticated;
    _user = null;
    notifyListeners();
  }

  void _setLoading(bool value) {
    _isLoading = value;
    notifyListeners();
  }

  @override
  void dispose() {
    _authEventSubscription.cancel();
    _authEventController.close();
    super.dispose();
  }
}
