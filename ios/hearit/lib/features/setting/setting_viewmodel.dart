import 'package:flutter/foundation.dart';

import '../../core/analytics/analytics_provider.dart';
import '../../core/analytics/analytics_event_names.dart';
import '../auth/auth_viewmodel.dart';
import 'models/member_profile.dart';
import 'setting_repository.dart';

class SettingViewModel extends ChangeNotifier {
  SettingViewModel({
    SettingRepository? repository,
    required AuthViewModel authViewModel,
  }) : _repository = repository ?? SettingRepository(),
       _authViewModel = authViewModel;

  final SettingRepository _repository;
  final AuthViewModel _authViewModel;

  MemberProfile? _profile;
  bool _isLoading = false;
  String? _errorMessage;

  MemberProfile? get profile => _profile;
  bool get isLoading => _isLoading;
  String? get errorMessage => _errorMessage;

  /// 내 프로필 정보 가져오기
  Future<void> fetchProfile() async {
    _setLoading(true);
    _errorMessage = null;

    try {
      _profile = await _repository.fetchMyProfile();
      _setLoading(false);
    } catch (error) {
      _errorMessage = error.toString().replaceAll('Exception: ', '');
      _profile = null;
      _setLoading(false);
      debugPrint('프로필 조회 실패: $error');
    }
  }

  /// 프로필 새로고침
  Future<void> refreshProfile() async {
    await fetchProfile();
  }

  void _setLoading(bool value) {
    _isLoading = value;
    notifyListeners();
  }

  /// 에러 메시지 초기화
  void clearError() {
    _errorMessage = null;
    notifyListeners();
  }

  /// 로그아웃
  Future<void> logout() async {
    _setLoading(true);
    _errorMessage = null;

    try {
      // Analytics 기록
      await AnalyticsProvider.logger.logEvent(
        AnalyticsEventNames.settingLogoutConfirmed,
      );

      // AuthViewModel의 logout 호출 (카카오 로그아웃 + 토큰 삭제)
      await _authViewModel.logout();

      _setLoading(false);
    } catch (error) {
      _errorMessage = '로그아웃에 실패했습니다.';
      _setLoading(false);
      debugPrint('로그아웃 실패: $error');
      rethrow;
    }
  }

  /// 회원탈퇴
  Future<void> withdraw() async {
    _setLoading(true);
    _errorMessage = null;

    try {
      // Analytics 기록
      await AnalyticsProvider.logger.logEvent(
        AnalyticsEventNames.settingWithdrawConfirmed,
      );

      // 회원탈퇴 API 호출
      await _repository.withdrawAccount();

      _setLoading(false);
    } catch (error) {
      _errorMessage = '회원탈퇴에 실패했습니다.';
      _setLoading(false);
      debugPrint('회원탈퇴 실패: $error');
      // API 실패해도 아래 finally에서 토큰 삭제됨
    } finally {
      // API 성공/실패 무관하게 로컬 토큰 삭제 및 인증 상태 초기화
      await _authViewModel.clearAuthStatus();
    }
  }
}
