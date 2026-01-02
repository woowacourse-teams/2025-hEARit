import 'package:flutter/material.dart';

import '../../../core/theme/app_colors.dart';

class GuestModeButton extends StatelessWidget {
  const GuestModeButton({super.key, required this.onPressed});

  final VoidCallback onPressed;

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onPressed,
      child: RichText(
        text: const TextSpan(
          style: TextStyle(
            fontSize: 14,
            fontWeight: FontWeight.w500,
            fontFamily: 'Pretendard',
            decoration: TextDecoration.underline,
            decorationColor: AppColors.gray4,
          ),
          children: [
            TextSpan(
              text: '회원가입 없이 ',
              style: TextStyle(color: AppColors.gray4),
            ),
            TextSpan(
              text: '히어릿',
              style: TextStyle(color: AppColors.hearitPurple1),
            ),
            TextSpan(
              text: '을 이용하고 싶어요',
              style: TextStyle(color: AppColors.gray4),
            ),
          ],
        ),
      ),
    );
  }
}
