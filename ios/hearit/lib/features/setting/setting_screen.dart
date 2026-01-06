import 'package:flutter/material.dart';
import 'package:hearit/core/theme/app_colors.dart';
import 'package:provider/provider.dart';
import 'package:url_launcher/url_launcher.dart';

import '../../core/analytics/analytics_provider.dart';
import '../../core/analytics/analytics_event_names.dart';
import '../auth/auth_viewmodel.dart';
import '../auth/login_screen.dart';
import 'my_profile_screen.dart';
import 'oss_licenses_page.dart';
import 'setting_viewmodel.dart';
import 'widgets/logout_confirm_dialog.dart';
import 'widgets/withdraw_confirm_dialog.dart';

class SettingScreen extends StatelessWidget {
  const SettingScreen({super.key, this.onBackToHome});

  final VoidCallback? onBackToHome;
  static const Color _backgroundColor = AppColors.hearitBlack;
  static const TextStyle _itemTextStyle = TextStyle(
    color: AppColors.gray4,
    fontSize: 16,
    fontWeight: FontWeight.w500,
  );

  static const String _privacyPolicyUrl =
      'https://glistening-eclipse-58b.notion.site/231d39b9c3c3809b9f92ec3e812ea24b';
  static const String _termsUrl =
      'https://glistening-eclipse-58b.notion.site/231d39b9c3c3800eb03cc7e1fc00f6f1';

  Future<void> _openExternalUrl(BuildContext context, String url) async {
    final uri = Uri.parse(url);
    final launched = await launchUrl(uri, mode: LaunchMode.externalApplication);
    if (!launched) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('링크를 열 수 없어요. 잠시 후 다시 시도해주세요.')),
      );
    }
  }

  void _handleBack(BuildContext context) {
    final canPop = Navigator.of(context).canPop();
    if (canPop) {
      Navigator.of(context).pop();
    } else {
      onBackToHome?.call();
    }
  }

  void _openLicenses(BuildContext context) {
    Navigator.of(
      context,
    ).push(MaterialPageRoute(builder: (_) => OssLicensesPage()));
  }

  void _openMyProfile(BuildContext context) {
    Navigator.of(
      context,
    ).push(MaterialPageRoute(builder: (_) => const MyProfileScreen()));
  }

  /// 로그아웃 처리
  Future<void> _handleLogout(BuildContext context) async {
    // Analytics: 로그아웃 버튼 클릭
    await AnalyticsProvider.logger.logEvent(
      AnalyticsEventNames.settingLogoutClicked,
    );

    // 확인 다이얼로그
    final confirmed = await LogoutConfirmDialog.show(context);
    if (!confirmed || !context.mounted) return;

    final settingViewModel = context.read<SettingViewModel>();

    try {
      // 로그아웃 실행
      await settingViewModel.logout();

      if (!context.mounted) return;

      // 성공 메시지 Toast
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('로그아웃이 완료되었습니다.'),
          backgroundColor: AppColors.hearitPurple2,
          duration: Duration(seconds: 2),
        ),
      );

      // 로그인 화면으로 이동 (루트 네비게이터 사용하여 모든 스택 제거)
      Navigator.of(context, rootNavigator: true).pushAndRemoveUntil(
        MaterialPageRoute(builder: (_) => const LoginScreen()),
        (route) => false,
      );
    } catch (error) {
      if (!context.mounted) return;

      // 실패 메시지
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(error.toString().replaceAll('Exception: ', '')),
          backgroundColor: AppColors.error,
          duration: const Duration(seconds: 2),
        ),
      );
    }
  }

  /// 회원탈퇴 처리
  Future<void> _handleWithdraw(BuildContext context) async {
    // Analytics: 회원탈퇴 버튼 클릭
    await AnalyticsProvider.logger.logEvent(
      AnalyticsEventNames.settingWithdrawClicked,
    );

    // 경고 다이얼로그
    final confirmed = await WithdrawConfirmDialog.show(context);
    if (!confirmed || !context.mounted) return;

    final settingViewModel = context.read<SettingViewModel>();

    try {
      // 회원탈퇴 실행
      await settingViewModel.withdraw();

      if (!context.mounted) return;

      // 성공 메시지 Toast
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('회원탈퇴가 완료되었습니다.'),
          backgroundColor: AppColors.hearitPurple2,
          duration: Duration(seconds: 2),
        ),
      );

      // 로그인 화면으로 이동 (루트 네비게이터 사용하여 모든 스택 제거)
      Navigator.of(context, rootNavigator: true).pushAndRemoveUntil(
        MaterialPageRoute(builder: (_) => const LoginScreen()),
        (route) => false,
      );
    } catch (error) {
      if (!context.mounted) return;

      // 실패 메시지 (API 실패해도 로컬 토큰은 이미 삭제됨)
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('회원탈퇴에 실패했습니다. 다시 로그인해주세요.'),
          backgroundColor: AppColors.error,
          duration: Duration(seconds: 2),
        ),
      );

      // API 실패해도 로그인 화면으로 이동 (루트 네비게이터 사용하여 모든 스택 제거)
      Navigator.of(context, rootNavigator: true).pushAndRemoveUntil(
        MaterialPageRoute(builder: (_) => const LoginScreen()),
        (route) => false,
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: _backgroundColor,
      appBar: AppBar(
        backgroundColor: _backgroundColor,
        elevation: 0,
        centerTitle: true,
        leading: IconButton(
          icon: const Icon(Icons.arrow_back_ios_new, color: Colors.white),
          onPressed: () => _handleBack(context),
        ),
        title: const Text(
          '설정',
          style: TextStyle(
            color: Colors.white,
            fontSize: 24,
            fontWeight: FontWeight.w600,
          ),
        ),
      ),
      body: SafeArea(
        child: Consumer<AuthViewModel>(
          builder: (context, authViewModel, _) {
            final isLoggedIn = authViewModel.isLoggedIn;

            return Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 20),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const SizedBox(height: 12),

                      // 로그인 상태일 때만 "내 정보" 표시
                      if (isLoggedIn) ...[
                        _SettingItem(
                          label: '내 정보',
                          onTap: () => _openMyProfile(context),
                        ),
                        const SizedBox(height: 20),
                      ],

                      _SettingItem(
                        label: '개인정보처리방침',
                        onTap: () =>
                            _openExternalUrl(context, _privacyPolicyUrl),
                      ),
                      const SizedBox(height: 20),
                      _SettingItem(
                        label: '이용 약관',
                        onTap: () => _openExternalUrl(context, _termsUrl),
                      ),
                      const SizedBox(height: 20),
                      _SettingItem(
                        label: '오픈 라이선스',
                        onTap: () => _openLicenses(context),
                      ),
                      const SizedBox(height: 20),
                    ],
                  ),
                ),

                // 로그인 상태일 때만 로그아웃/회원탈퇴 표시
                if (isLoggedIn) ...[
                  const SizedBox(height: 12),
                  Container(
                    width: double.infinity,
                    height: 1,
                    color: AppColors.darkGray,
                  ),
                  const SizedBox(height: 12),
                  Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 20),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const SizedBox(height: 20),
                        _SettingItem(
                          label: '로그아웃',
                          onTap: () => _handleLogout(context),
                          textStyle: _itemTextStyle.copyWith(
                            color: AppColors.gray4,
                            fontWeight: FontWeight.w500,
                          ),
                        ),
                        const SizedBox(height: 20),
                        _SettingItem(
                          label: '회원탈퇴',
                          onTap: () => _handleWithdraw(context),
                          textStyle: _itemTextStyle.copyWith(
                            color: AppColors.error,
                            fontWeight: FontWeight.w500,
                          ),
                        ),
                      ],
                    ),
                  ),
                ] else ...[
                  const SizedBox(height: 12),
                  Container(
                    width: double.infinity,
                    height: 1,
                    color: AppColors.darkGray,
                  ),
                  const SizedBox(height: 12),
                  Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 20),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const SizedBox(height: 20),
                        _SettingItem(
                          label: '로그인 하러가기',
                          onTap: () {
                            Navigator.of(
                              context,
                              rootNavigator: true,
                            ).pushAndRemoveUntil(
                              MaterialPageRoute(
                                builder: (_) => const LoginScreen(),
                              ),
                              (route) => false,
                            );
                          },
                          textStyle: _itemTextStyle.copyWith(
                            color: AppColors.gray4,
                            fontWeight: FontWeight.w500,
                          ),
                        ),
                      ],
                    ),
                  ),
                ],
                const Spacer(),
                Center(
                  child: Padding(
                    padding: const EdgeInsets.only(bottom: 24),
                    child: _SettingItem(
                      label: '피드백 및 문의하기',
                      onTap: () => _openExternalUrl(
                        context,
                        'https://forms.gle/KGjHNi9ASdN3jR5v6',
                      ),
                      textStyle: _itemTextStyle.copyWith(
                        color: Colors.white,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                  ),
                ),
              ],
            );
          },
        ),
      ),
    );
  }
}

class _SettingItem extends StatelessWidget {
  const _SettingItem({required this.label, this.onTap, this.textStyle});

  final String label;
  final VoidCallback? onTap;
  final TextStyle? textStyle;

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      splashColor: Colors.white12,
      highlightColor: Colors.white10,
      borderRadius: BorderRadius.circular(4),
      child: Padding(
        padding: const EdgeInsets.symmetric(vertical: 2),
        child: Text(label, style: textStyle ?? SettingScreen._itemTextStyle),
      ),
    );
  }
}
