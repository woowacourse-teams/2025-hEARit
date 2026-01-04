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
        margin: const EdgeInsets.only(left: 20, right: 4, top: 0, bottom: 0),
        padding: const EdgeInsets.symmetric(horizontal: 0, vertical: 8),
        decoration: BoxDecoration(
          color: Colors.transparent, // 배경 투명
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
                  const SizedBox(height: 8),
                  _buildTitleRow(), // 제목 + 체크 + 재생시간 한 줄
                  const SizedBox(height: 12),
                  _buildProgressBar(), // 진행률 바만
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
      width: 60,
      height: 60,
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

  /// 제목 행 (제목 + 완료 아이콘 + 재생시간)
  Widget _buildTitleRow() {
    return Row(
      children: [
        Expanded(
          child: Text(
            hearit.title,
            style: const TextStyle(
              color: Colors.white,
              fontSize: 14,
              fontWeight: FontWeight.w600,
            ),
            maxLines: 1,
            overflow: TextOverflow.ellipsis,
          ),
        ),
        if (hearit.isFinished) ...[
          const SizedBox(width: 4),
          Image.asset(
            'assets/images/finished_check.png',
            width: 20,
            height: 20,
          ),
        ],
        const SizedBox(width: 8),
        Text(
          hearit.formattedPlayTime,
          style: const TextStyle(
            color: AppColors.gray4,
            fontSize: 12,
            fontWeight: FontWeight.w500,
          ),
        ),
      ],
    );
  }

  /// 진행률 바
  Widget _buildProgressBar() {
    return ClipRRect(
      borderRadius: BorderRadius.circular(4),
      child: LinearProgressIndicator(
        value: hearit.progress,
        minHeight: 4,
        backgroundColor: const Color(0xFF44474B),
        valueColor: const AlwaysStoppedAnimation<Color>(
          AppColors.hearitPurple2,
        ),
      ),
    );
  }

  /// 메뉴 버튼 (점 3개)
  Widget _buildMenuButton() {
    return IconButton(
      icon: const Icon(Icons.more_vert, color: AppColors.gray4, size: 28),
      onPressed: onMenuTap,
      padding: EdgeInsets.zero,
      constraints: const BoxConstraints(minWidth: 28, minHeight: 28),
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
