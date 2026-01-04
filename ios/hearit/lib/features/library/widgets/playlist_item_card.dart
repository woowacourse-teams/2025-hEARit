import 'package:flutter/material.dart';
import 'package:hearit/core/theme/app_colors.dart';

import '../playlist_models.dart';

/// 플레이리스트 항목 카드
///
/// 좌측: 카테고리 색상 아이콘
/// 중앙: 제목 + "sourceName • MM:SS"
/// 우측: 재생/일시정지 버튼
class PlaylistItemCard extends StatelessWidget {
  final PlaylistItem item;
  final bool isPlaying;
  final VoidCallback onTap;

  const PlaylistItemCard({
    super.key,
    required this.item,
    required this.isPlaying,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
        decoration: BoxDecoration(
          color: isPlaying
              ? AppColors.hearitPurple3.withValues(alpha: 0.2)
              : Colors.transparent,
        ),
        child: Row(
          children: [
            // 좌측: 카테고리 색상 아이콘
            _CategoryIcon(colorCode: item.categoryColorCode),
            const SizedBox(width: 12),

            // 중앙: 제목 + 소스명 + 재생시간
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    item.title,
                    style: TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.w600,
                      color: AppColors.hearitBlack,
                    ),
                    maxLines: 2,
                    overflow: TextOverflow.ellipsis,
                  ),
                  const SizedBox(height: 4),
                  Text(
                    '${item.sourceName} • ${item.formattedDuration}',
                    style: const TextStyle(
                      fontSize: 14,
                      color: AppColors.darkGray,
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(width: 12),

            // 우측: 재생/일시정지 버튼
            Icon(
              isPlaying ? Icons.pause_circle_filled : Icons.play_circle_filled,
              size: 32,
              color: AppColors.hearitPurple3,
            ),
          ],
        ),
      ),
    );
  }
}

/// 카테고리 색상 아이콘 (64x64)
class _CategoryIcon extends StatelessWidget {
  final String colorCode;

  const _CategoryIcon({required this.colorCode});

  /// #RRGGBB 형식의 색상 코드를 Color 객체로 변환
  Color _parseColor(String hexColor) {
    try {
      // #RRGGBB → 0xFFRRGGBB
      final hex = hexColor.replaceAll('#', '');
      return Color(int.parse('FF$hex', radix: 16));
    } catch (e) {
      // 파싱 실패 시 기본 색상 (회색)
      return AppColors.gray3;
    }
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 64,
      height: 64,
      decoration: BoxDecoration(
        color: _parseColor(colorCode),
        borderRadius: BorderRadius.circular(8),
      ),
      child: const Icon(Icons.album_rounded, size: 32, color: Colors.white70),
    );
  }
}
