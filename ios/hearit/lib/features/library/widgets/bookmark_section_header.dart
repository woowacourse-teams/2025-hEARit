import 'package:flutter/material.dart';
import 'package:hearit/core/theme/app_colors.dart';

/// 북마크 섹션 헤더
/// "내가 북마크한 히어릿" 타이틀 + 개수 + 전체재생 버튼
class BookmarkSectionHeader extends StatelessWidget {
  const BookmarkSectionHeader({
    super.key,
    required this.totalCount,
    this.onPlayAll,
  });

  final int totalCount;
  final VoidCallback? onPlayAll;

  @override
  Widget build(BuildContext context) {
    final hasBookmarks = totalCount > 0;
    final showPlayButton = onPlayAll != null; // onPlayAll이 null이 아니면 버튼 표시

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 4),
      child: Row(
        children: [
          // 왼쪽: 타이틀 + 개수
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text(
                  '내가 북마크한 히어릿',
                  style: TextStyle(
                    color: Colors.white,
                    fontSize: 20,
                    fontWeight: FontWeight.w700,
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  '히어릿 $totalCount개',
                  style: TextStyle(
                    color: Colors.white.withOpacity(0.6),
                    fontSize: 14,
                    fontWeight: FontWeight.w500,
                  ),
                ),
              ],
            ),
          ),
          // 오른쪽: 전체재생 버튼 (onPlayAll이 있으면 표시)
          if (showPlayButton)
            GestureDetector(
              onTap: onPlayAll,
              child: Container(
                width: 56,
                height: 56,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  color: AppColors.hearitPurple3,
                  boxShadow: [
                    BoxShadow(
                      color: AppColors.hearitPurple3.withOpacity(0.4),
                      blurRadius: 12,
                      offset: const Offset(0, 4),
                    ),
                  ],
                ),
                child: const Icon(
                  Icons.play_arrow_rounded,
                  color: Colors.white,
                  size: 32,
                ),
              ),
            ),
        ],
      ),
    );
  }
}
