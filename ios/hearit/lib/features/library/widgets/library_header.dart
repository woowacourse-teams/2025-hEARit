import 'package:flutter/material.dart';
import 'package:hearit/core/theme/app_colors.dart';

import '../../setting/models/member_profile.dart';
import '../../setting/setting_screen.dart';

/// 라이브러리 화면 상단 프로필 헤더
/// 프로필 이미지 + 닉네임 + 그라데이션 배경 + 설정 버튼
class LibraryHeader extends StatelessWidget {
  const LibraryHeader({super.key, required this.profile, this.isGuest = false});

  final MemberProfile? profile;
  final bool isGuest;

  void _navigateToSettings(BuildContext context) {
    Navigator.of(
      context,
    ).push(MaterialPageRoute(builder: (_) => const SettingScreen()));
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.topCenter,
          end: Alignment.bottomCenter,
          colors: [
            AppColors.hearitPurple3,
            AppColors.hearitPurple3.withOpacity(0.95),
            AppColors.hearitPurple3.withOpacity(0.85),
            AppColors.hearitPurple3.withOpacity(0.7),
            AppColors.hearitPurple3.withOpacity(0.5),
            AppColors.hearitPurple3.withOpacity(0.25),
            AppColors.hearitPurple3.withOpacity(0.05),
            Colors.transparent,
          ],
          stops: const [0.0, 0.2, 0.35, 0.5, 0.65, 0.8, 0.95, 1.0],
        ),
      ),
      child: SafeArea(
        bottom: false,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // 설정 아이콘 (상단에 배치)
            Align(
              alignment: Alignment.topRight,
              child: Padding(
                padding: const EdgeInsets.only(right: 8, top: 4),
                child: IconButton(
                  icon: const Icon(
                    Icons.settings,
                    color: Colors.white,
                    size: 28,
                  ),
                  onPressed: () => _navigateToSettings(context),
                ),
              ),
            ),
            // 프로필 영역 (위로 올림)
            Padding(
              padding: const EdgeInsets.only(
                left: 20,
                right: 20,
                top: 0,
                bottom: 28,
              ),
              child: Row(
                children: [
                  // 프로필 이미지
                  _buildProfileImage(),
                  const SizedBox(width: 16),
                  // 닉네임
                  Expanded(
                    child: Text(
                      isGuest ? 'hEARit' : (profile?.nickname ?? '사용자'),
                      style: const TextStyle(
                        color: Colors.white,
                        fontSize: 22,
                        fontWeight: FontWeight.w700,
                      ),
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildProfileImage() {
    // Guest users always see 'H' initial on HearitPurple3 background
    if (isGuest) {
      return Container(
        width: 48,
        height: 48,
        decoration: BoxDecoration(
          shape: BoxShape.circle,
          color: AppColors.hearitPurple3,
          boxShadow: [
            BoxShadow(
              color: Colors.black.withOpacity(0.2),
              blurRadius: 8,
              offset: const Offset(0, 2),
            ),
          ],
        ),
        child: Center(
          child: Text(
            'H',
            style: const TextStyle(
              color: AppColors.gray4,
              fontSize: 28,
              fontWeight: FontWeight.w700,
            ),
          ),
        ),
      );
    }

    // Logged-in users: existing logic
    final hasImage = profile?.hasProfileImage ?? false;

    return Container(
      width: 48,
      height: 48,
      decoration: BoxDecoration(
        shape: BoxShape.circle,
        color: hasImage ? Colors.grey[300] : AppColors.hearitPurple2,
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.2),
            blurRadius: 8,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: hasImage
          ? ClipOval(
              child: Image.network(
                profile!.profileImage!,
                fit: BoxFit.cover,
                errorBuilder: (context, error, stackTrace) {
                  // 이미지 로드 실패 시 이니셜 표시
                  return _buildInitial();
                },
              ),
            )
          : _buildInitial(),
    );
  }

  Widget _buildInitial() {
    return Center(
      child: Text(
        profile?.initial ?? 'H',
        style: const TextStyle(
          color: Colors.white,
          fontSize: 28,
          fontWeight: FontWeight.w700,
        ),
      ),
    );
  }
}
