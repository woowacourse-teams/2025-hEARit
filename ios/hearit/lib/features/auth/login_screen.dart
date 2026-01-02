import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../core/theme/app_colors.dart';
import '../../core/presentation/main_navigation.dart';
import 'auth_viewmodel.dart';
import 'widgets/kakao_login_button.dart';
import 'widgets/guest_mode_button.dart';

class LoginScreen extends StatelessWidget {
  const LoginScreen({super.key});

  Future<void> _handleKakaoLogin(BuildContext context) async {
    final authViewModel = context.read<AuthViewModel>();

    try {
      await authViewModel.loginWithKakao();

      if (!context.mounted) return;

      // 로그인 성공 -> 홈 화면으로 이동
      Navigator.of(context).pushReplacement(
        MaterialPageRoute(builder: (_) => const MainNavigation()),
      );
    } catch (error) {
      if (!context.mounted) return;

      // 에러 메시지 표시
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(error.toString().replaceAll('Exception: ', '')),
          backgroundColor: Colors.red,
          duration: const Duration(seconds: 3),
        ),
      );
    }
  }

  Future<void> _handleGuestMode(BuildContext context) async {
    final authViewModel = context.read<AuthViewModel>();
    await authViewModel.enterGuestMode();

    if (!context.mounted) return;

    // 게스트 모드 진입 -> 홈 화면으로 이동
    Navigator.of(context).pushReplacement(
      MaterialPageRoute(builder: (_) => const MainNavigation()),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.hearitBlack,
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 24),
          child: Column(
            children: [
              const Spacer(flex: 2),

              // hEARit 앱 아이콘 (중앙)
              Image.asset('assets/icon/app_icon.png', width: 120, height: 120),

              const Spacer(flex: 3),

              // 카카오 로그인 버튼
              Consumer<AuthViewModel>(
                builder: (context, authViewModel, _) {
                  return KakaoLoginButton(
                    onPressed: () => _handleKakaoLogin(context),
                    isLoading: authViewModel.isLoading,
                  );
                },
              ),

              const SizedBox(height: 16),

              // 게스트 모드 버튼
              GuestModeButton(onPressed: () => _handleGuestMode(context)),

              const SizedBox(height: 40),
            ],
          ),
        ),
      ),
    );
  }
}
