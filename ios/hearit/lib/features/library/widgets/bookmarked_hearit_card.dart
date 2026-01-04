import 'package:flutter/material.dart';
import 'package:hearit/core/theme/app_colors.dart';

import '../library_models.dart';

/// 북마크된 히어릿 카드
/// 카테고리 표지 + 히어릿 정보 + 진행률 바 + 메뉴 버튼
class BookmarkedHearitCard extends StatelessWidget {
  const BookmarkedHearitCard({
    super.key,
    required this.hearit,
    this.onTap,
    this.onMenuTap,
  });

  final BookmarkedHearit hearit;
  final VoidCallback? onTap;
  final VoidCallback? onMenuTap;

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        margin: const EdgeInsets.symmetric(horizontal: 20, vertical: 8),
        padding: const EdgeInsets.all(12),
        decoration: BoxDecoration(
          color: AppColors.gray1,
          borderRadius: BorderRadius.circular(12),
        ),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // 카테고리 표지
            _buildCategoryBadge(),
            const SizedBox(width: 12),
            // 히어릿 정보
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _buildTitleRow(),
                  const SizedBox(height: 6),
                  _buildInfoRow(),
                  const SizedBox(height: 8),
                  _buildProgressBar(),
                ],
              ),
            ),
            // 메뉴 버튼
            _buildMenuButton(),
          ],
        ),
      ),
    );
  }

  /// 카테고리 표지 (색상 + 이니셜)
  Widget _buildCategoryBadge() {
    final categoryColor = _parseColor(hearit.category.colorCode);

    return Container(
      width: 64,
      height: 64,
      decoration: BoxDecoration(
        color: categoryColor,
        borderRadius: BorderRadius.circular(8),
      ),
      child: Center(
        child: Text(
          hearit.category.initial,
          style: const TextStyle(
            color: Colors.white,
            fontSize: 28,
            fontWeight: FontWeight.w700,
          ),
        ),
      ),
    );
  }

  /// 제목 행 (제목 + 완료 아이콘)
  Widget _buildTitleRow() {
    return Row(
      children: [
        Expanded(
          child: Text(
            hearit.title,
            style: const TextStyle(
              color: Colors.white,
              fontSize: 16,
              fontWeight: FontWeight.w600,
            ),
            maxLines: 2,
            overflow: TextOverflow.ellipsis,
          ),
        ),
        if (hearit.isFinished) ...[
          const SizedBox(width: 8),
          Container(
            padding: const EdgeInsets.all(4),
            decoration: BoxDecoration(
              color: AppColors.hearitPurple2,
              shape: BoxShape.circle,
            ),
            child: const Icon(Icons.check, color: Colors.white, size: 16),
          ),
        ],
      ],
    );
  }

  /// 정보 행 (재생 시간)
  Widget _buildInfoRow() {
    return Row(
      children: [
        Icon(Icons.access_time, color: Colors.white.withOpacity(0.6), size: 14),
        const SizedBox(width: 4),
        Text(
          hearit.formattedPlayTime,
          style: TextStyle(
            color: Colors.white.withOpacity(0.6),
            fontSize: 14,
            fontWeight: FontWeight.w500,
          ),
        ),
      ],
    );
  }

  /// 진행률 바
  Widget _buildProgressBar() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        ClipRRect(
          borderRadius: BorderRadius.circular(4),
          child: LinearProgressIndicator(
            value: hearit.progress,
            minHeight: 6,
            backgroundColor: AppColors.gray2,
            valueColor: const AlwaysStoppedAnimation<Color>(
              AppColors.hearitPurple2,
            ),
          ),
        ),
        const SizedBox(height: 4),
        Text(
          '${(hearit.progress * 100).toStringAsFixed(0)}% 완료',
          style: TextStyle(
            color: Colors.white.withOpacity(0.5),
            fontSize: 12,
            fontWeight: FontWeight.w500,
          ),
        ),
      ],
    );
  }

  /// 메뉴 버튼 (점 3개)
  Widget _buildMenuButton() {
    return IconButton(
      icon: Icon(
        Icons.more_vert,
        color: Colors.white.withOpacity(0.7),
        size: 24,
      ),
      onPressed: onMenuTap,
      padding: EdgeInsets.zero,
      constraints: const BoxConstraints(),
    );
  }

  /// 색상 코드를 Color로 변환
  Color _parseColor(String colorCode) {
    try {
      // "#RRGGBB" 형식을 처리
      final hexColor = colorCode.replaceAll('#', '');
      return Color(int.parse('FF$hexColor', radix: 16));
    } catch (e) {
      // 파싱 실패 시 기본 색상 반환
      return AppColors.hearitPurple2;
    }
  }
}
