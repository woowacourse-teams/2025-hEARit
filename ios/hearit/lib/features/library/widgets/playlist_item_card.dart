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
  final bool isCurrentItem; // 현재 재생 목록의 항목인지
  final bool isPlaying; // 실제 재생 중인지 (일시정지 아님)
  final VoidCallback onTap;
  final VoidCallback onPlayPauseTap;

  const PlaylistItemCard({
    super.key,
    required this.item,
    required this.isCurrentItem,
    required this.isPlaying,
    required this.onTap,
    required this.onPlayPauseTap,
  });

  /// sourceName을 포맷팅
  /// 1. ':'가 있으면 ':' 이전까지만 표시
  /// 2. 15자 초과 시 '...'로 생략
  String _formatSourceName(String sourceName) {
    String formatted = sourceName;

    // ':' 구분자가 있으면 ':' 이전까지만 사용
    if (formatted.contains(':')) {
      formatted = formatted.split(':').first;
    }

    // 15자 초과 시 생략
    if (formatted.length > 15) {
      formatted = '${formatted.substring(0, 15)}...';
    }

    return formatted;
  }

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
        decoration: BoxDecoration(
          color: isCurrentItem ? const Color(0xFF795B9B) : AppColors.gray1,
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
                    style: const TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.w600,
                      color: AppColors.gray4,
                    ),
                    maxLines: 2,
                    overflow: TextOverflow.ellipsis,
                  ),
                  const SizedBox(height: 4),
                  Text(
                    '${_formatSourceName(item.sourceName)} • ${item.formattedDuration}',
                    style: const TextStyle(
                      fontSize: 14,
                      color: AppColors.gray2,
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(width: 12),

            // 우측: 재생/일시정지 버튼 (독립적인 GestureDetector)
            GestureDetector(
              onTap: onPlayPauseTap,
              behavior: HitTestBehavior.opaque,
              child: Padding(
                padding: const EdgeInsets.all(4.0), // 클릭 영역 확대
                child: Icon(
                  // 현재 항목이면서 재생 중일 때만 일시정지 아이콘
                  (isCurrentItem && isPlaying) ? Icons.pause : Icons.play_arrow,
                  size: 32,
                  color: AppColors.gray4,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

/// 카테고리 색상 아이콘 (64x64) with LP.png overlay
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
      child: Center(
        child: ClipRRect(
          borderRadius: BorderRadius.circular(8),
          child: Image.asset(
            'assets/images/LP.png',
            width: 40,
            height: 40,
            fit: BoxFit.cover,
          ),
        ),
      ),
    );
  }
}
