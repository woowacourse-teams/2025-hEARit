import 'package:flutter/material.dart';
import 'package:hearit/core/theme/app_colors.dart';

import '../home_font.dart';

class HomeHeader extends StatelessWidget {
  const HomeHeader({super.key});

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.start,
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'hEARit',
              style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                    fontFamily: homeTitleFontFamily,
                    fontWeight: FontWeight.w900,
                    color: AppColors.gray4,
                  ),
            ),
            const SizedBox(height: 4),
            Text(
              'IT를 쉽게 들을 수 있는 팟캐스트, 히어릿',
              style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                    color: AppColors.gray4.withOpacity(0.75),
                    fontSize: 15,
                  ),
            ),
          ],
        ),
      ],
    );
  }
}
