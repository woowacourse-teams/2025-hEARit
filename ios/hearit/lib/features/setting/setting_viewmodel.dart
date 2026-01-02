import 'package:flutter/foundation.dart';

import 'models/member_profile.dart';
import 'setting_repository.dart';

class SettingViewModel extends ChangeNotifier {
  SettingViewModel({SettingRepository? repository})
    : _repository = repository ?? SettingRepository();

  final SettingRepository _repository;

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
}
