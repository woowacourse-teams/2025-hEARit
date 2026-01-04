import 'package:flutter/material.dart';

import '../../../core/theme/app_colors.dart';

class BookmarkButton extends StatelessWidget {
  const BookmarkButton({
    super.key,
    required this.isBookmarked,
    required this.onPressed,
  });

  final bool isBookmarked;
  final VoidCallback onPressed;

  @override
  Widget build(BuildContext context) {
    return IconButton(
      iconSize: 40,
      onPressed: onPressed,
      icon: Icon(
        isBookmarked ? Icons.bookmark : Icons.bookmark_border,
        color: isBookmarked ? AppColors.hearitPurple3 : AppColors.gray4,
      ),
    );
  }
}
