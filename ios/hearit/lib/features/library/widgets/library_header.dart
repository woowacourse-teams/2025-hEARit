import 'package:flutter/material.dart';
import 'package:hearit/core/theme/app_colors.dart';

import '../../setting/models/member_profile.dart';
import '../../setting/setting_screen.dart';

/// 라이브러리 화면 상단 프로필 헤더
/// 프로필 이미지 + 닉네임 + 그라데이션 배경 + 설정 버튼
class LibraryHeader extends StatelessWidget {
  const LibraryHeader({super.key, required this.profile});

  final MemberProfile? profile;

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
            AppColors.hearitPurple3.withOpacity(0.7),
            AppColors.hearitPurple3.withOpacity(0.3),
            Colors.transparent,
          ],
          stops: const [0.0, 0.4, 0.7, 1.0],
        ),
      ),
      child: SafeArea(
        bottom: false,
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
          child: Row(
            children: [
              // 프로필 이미지
              _buildProfileImage(),
              const SizedBox(width: 16),
              // 닉네임
              Expanded(
                child: Text(
                  profile?.nickname ?? '사용자',
                  style: const TextStyle(
                    color: Colors.white,
                    fontSize: 24,
                    fontWeight: FontWeight.w700,
                  ),
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                ),
              ),
              // 설정 버튼
              IconButton(
                icon: const Icon(Icons.settings, color: Colors.white, size: 28),
                onPressed: () => _navigateToSettings(context),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildProfileImage() {
    final hasImage = profile?.hasProfileImage ?? false;

    return Container(
      width: 60,
      height: 60,
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
