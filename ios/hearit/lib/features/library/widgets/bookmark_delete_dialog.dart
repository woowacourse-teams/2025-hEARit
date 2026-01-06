import 'package:flutter/material.dart';
import 'package:hearit/core/theme/app_colors.dart';

/// 북마크 삭제 확인 다이얼로그
class BookmarkDeleteDialog {
  /// 삭제 확인 다이얼로그 표시
  static Future<bool> show(
    BuildContext context, {
    required String hearitTitle,
  }) async {
    final result = await showDialog<bool>(
      context: context,
      builder: (context) =>
          _BookmarkDeleteDialogContent(hearitTitle: hearitTitle),
    );

    return result ?? false;
  }
}

class _BookmarkDeleteDialogContent extends StatelessWidget {
  const _BookmarkDeleteDialogContent({required this.hearitTitle});

  final String hearitTitle;

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      backgroundColor: AppColors.gray1,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      title: const Text(
        '북마크 삭제',
        style: TextStyle(
          color: Colors.white,
          fontSize: 20,
          fontWeight: FontWeight.w700,
        ),
      ),
      content: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            '이 팟캐스트를 북마크에서 삭제하시겠습니까?',
            style: TextStyle(
              color: Colors.white.withOpacity(0.8),
              fontSize: 16,
            ),
          ),
          const SizedBox(height: 12),
          Container(
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              color: AppColors.hearitBlack,
              borderRadius: BorderRadius.circular(8),
            ),
            child: Text(
              hearitTitle,
              style: const TextStyle(
                color: AppColors.hearitPurple2,
                fontSize: 14,
                fontWeight: FontWeight.w600,
              ),
              maxLines: 2,
              overflow: TextOverflow.ellipsis,
            ),
          ),
        ],
      ),
      actions: [
        // 취소 버튼
        TextButton(
          onPressed: () => Navigator.of(context).pop(false),
          child: Text(
            '취소',
            style: TextStyle(
              color: Colors.white.withOpacity(0.7),
              fontSize: 16,
              fontWeight: FontWeight.w600,
            ),
          ),
        ),
        // 삭제 버튼
        TextButton(
          onPressed: () => Navigator.of(context).pop(true),
          child: const Text(
            '삭제',
            style: TextStyle(
              color: AppColors.error,
              fontSize: 16,
              fontWeight: FontWeight.w700,
            ),
          ),
        ),
      ],
    );
  }
}

/// 북마크 삭제 메뉴 BottomSheet
class BookmarkMenuSheet {
  /// 북마크 메뉴 BottomSheet 표시
  static Future<BookmarkMenuAction?> show(
    BuildContext context, {
    required String hearitTitle,
  }) async {
    return await showModalBottomSheet<BookmarkMenuAction>(
      context: context,
      backgroundColor: Colors.transparent,
      useRootNavigator: true,
      builder: (context) => _BookmarkMenuSheetContent(hearitTitle: hearitTitle),
    );
  }
}

enum BookmarkMenuAction { delete }

class _BookmarkMenuSheetContent extends StatelessWidget {
  const _BookmarkMenuSheetContent({required this.hearitTitle});

  final String hearitTitle;

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: const BoxDecoration(
        color: AppColors.gray1,
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          // 핸들
          Container(
            margin: const EdgeInsets.only(top: 8, bottom: 4),
            width: 40,
            height: 4,
            decoration: BoxDecoration(
              color: Colors.white.withOpacity(0.3),
              borderRadius: BorderRadius.circular(2),
            ),
          ),
          // 타이틀
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 8),
            child: Text(
              hearitTitle,
              style: const TextStyle(
                color: Colors.white,
                fontSize: 16,
                fontWeight: FontWeight.w600,
              ),
              maxLines: 2,
              overflow: TextOverflow.ellipsis,
              textAlign: TextAlign.center,
            ),
          ),
          // 삭제 메뉴
          ListTile(
            leading: const Icon(Icons.delete_outline, color: AppColors.error),
            title: const Text(
              '북마크 삭제',
              style: TextStyle(
                color: AppColors.error,
                fontSize: 16,
                fontWeight: FontWeight.w600,
              ),
            ),
            onTap: () {
              Navigator.of(context).pop(BookmarkMenuAction.delete);
            },
          ),
          // 하단 SafeArea 패딩 (네비게이션 바 고려)
          SizedBox(height: MediaQuery.of(context).padding.bottom + 36),
        ],
      ),
    );
  }
}
