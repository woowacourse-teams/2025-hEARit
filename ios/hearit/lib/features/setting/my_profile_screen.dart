import 'package:flutter/material.dart';
import 'package:package_info_plus/package_info_plus.dart';
import 'package:provider/provider.dart';

import '../../core/theme/app_colors.dart';
import 'models/member_profile.dart';
import 'setting_viewmodel.dart';

class MyProfileScreen extends StatefulWidget {
  const MyProfileScreen({super.key});

  @override
  State<MyProfileScreen> createState() => _MyProfileScreenState();
}

class _MyProfileScreenState extends State<MyProfileScreen> {
  String _appVersion = '';

  @override
  void initState() {
    super.initState();
    _loadAppVersion();
    // 프로필 정보 가져오기
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<SettingViewModel>().fetchProfile();
    });
  }

  Future<void> _loadAppVersion() async {
    final packageInfo = await PackageInfo.fromPlatform();
    setState(() {
      _appVersion = 'v ${packageInfo.version}';
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.hearitBlack,
      appBar: AppBar(
        backgroundColor: AppColors.hearitBlack,
        elevation: 0,
        centerTitle: true,
        leading: IconButton(
          icon: const Icon(Icons.arrow_back_ios_new, color: Colors.white),
          onPressed: () => Navigator.of(context).pop(),
        ),
        title: const Text(
          '내정보',
          style: TextStyle(
            color: Colors.white,
            fontSize: 20,
            fontWeight: FontWeight.w600,
          ),
        ),
      ),
      body: Consumer<SettingViewModel>(
        builder: (context, viewModel, _) {
          // 로딩 중
          if (viewModel.isLoading) {
            return const Center(
              child: CircularProgressIndicator(
                valueColor: AlwaysStoppedAnimation<Color>(
                  AppColors.hearitPurple1,
                ),
              ),
            );
          }

          // 에러 발생
          if (viewModel.errorMessage != null) {
            return Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Text(
                    viewModel.errorMessage!,
                    style: const TextStyle(color: Colors.white70, fontSize: 16),
                    textAlign: TextAlign.center,
                  ),
                  const SizedBox(height: 16),
                  ElevatedButton(
                    onPressed: () => viewModel.fetchProfile(),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: AppColors.hearitPurple2,
                    ),
                    child: const Text('다시 시도'),
                  ),
                ],
              ),
            );
          }

          // 프로필이 없는 경우
          if (viewModel.profile == null) {
            return const Center(
              child: Text(
                '프로필 정보를 불러올 수 없습니다.',
                style: TextStyle(color: Colors.white70, fontSize: 16),
              ),
            );
          }

          // 정상 표시
          return _buildProfileContent(viewModel.profile!);
        },
      ),
    );
  }

  Widget _buildProfileContent(MemberProfile profile) {
    return SafeArea(
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 24),
        child: Column(
          children: [
            const SizedBox(height: 60),

            // 프로필 아바타
            _ProfileAvatar(profile: profile),

            const SizedBox(height: 32),

            // 카톡 닉네임 버튼
            _NicknameButton(nickname: profile.nickname),

            const Spacer(),

            // 하단 정보 (앱 버전, 이메일)
            _buildBottomInfo(),

            const SizedBox(height: 24),
          ],
        ),
      ),
    );
  }

  Widget _buildBottomInfo() {
    return Column(
      children: [
        Text(
          '앱 버전 $_appVersion',
          style: const TextStyle(
            color: Colors.white60,
            fontSize: 13,
            fontWeight: FontWeight.w400,
          ),
        ),
        const SizedBox(height: 4),
        const Text(
          'hearit2025@gmail.com',
          style: TextStyle(
            color: Colors.white60,
            fontSize: 13,
            fontWeight: FontWeight.w400,
          ),
        ),
      ],
    );
  }
}

/// 프로필 아바타 (이미지 또는 닉네임 첫 글자)
class _ProfileAvatar extends StatelessWidget {
  const _ProfileAvatar({required this.profile});

  final MemberProfile profile;

  @override
  Widget build(BuildContext context) {
    // 프로필 이미지가 있는 경우
    if (profile.hasProfileImage) {
      return ClipOval(
        child: Image.network(
          profile.profileImage!,
          width: 120,
          height: 120,
          fit: BoxFit.cover,
          errorBuilder: (context, error, stackTrace) {
            // 이미지 로드 실패 시 닉네임 첫 글자 표시
            return _buildInitialAvatar();
          },
          loadingBuilder: (context, child, loadingProgress) {
            if (loadingProgress == null) return child;
            return Container(
              width: 120,
              height: 120,
              decoration: const BoxDecoration(
                color: AppColors.hearitPurple2,
                shape: BoxShape.circle,
              ),
              child: const Center(
                child: CircularProgressIndicator(
                  valueColor: AlwaysStoppedAnimation<Color>(Colors.white),
                  strokeWidth: 2,
                ),
              ),
            );
          },
        ),
      );
    }

    // 프로필 이미지가 없는 경우 - 닉네임 첫 글자 표시
    return _buildInitialAvatar();
  }

  Widget _buildInitialAvatar() {
    return Container(
      width: 120,
      height: 120,
      decoration: const BoxDecoration(
        color: AppColors.hearitPurple2,
        shape: BoxShape.circle,
      ),
      child: Center(
        child: Text(
          profile.initial,
          style: const TextStyle(
            color: Colors.white,
            fontSize: 48,
            fontWeight: FontWeight.w700,
            fontFamily: 'Pretendard',
          ),
        ),
      ),
    );
  }
}

/// 카톡 닉네임 버튼 (정보 표시용 - 클릭 불가)
class _NicknameButton extends StatelessWidget {
  const _NicknameButton({required this.nickname});

  final String nickname;

  @override
  Widget build(BuildContext context) {
    return Container(
      height: 48,
      padding: const EdgeInsets.symmetric(horizontal: 24),
      decoration: BoxDecoration(
        color: Colors.transparent,
        border: Border.all(color: AppColors.hearitPurple2, width: 1.5),
        borderRadius: BorderRadius.circular(24),
      ),
      child: Center(
        child: Text(
          nickname,
          style: const TextStyle(
            color: Colors.white,
            fontSize: 16,
            fontWeight: FontWeight.w500,
            fontFamily: 'Pretendard',
          ),
        ),
      ),
    );
  }
}
