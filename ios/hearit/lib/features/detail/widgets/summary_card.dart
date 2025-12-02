import 'package:flutter/material.dart';
import 'package:hearit/core/theme/app_colors.dart';

import '../detail_font.dart';
import '../hearit_detail.dart';

class SummaryCard extends StatelessWidget {
  const SummaryCard({super.key, required this.summary, required this.keywords});

  final String summary;
  final List<HearitKeyword> keywords;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: AppColors.gray1,
        borderRadius: BorderRadius.circular(8),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            '요약',
            style: Theme.of(context).textTheme.titleSmall?.copyWith(
              fontFamily: detailFontFamily,
              color: AppColors.gray3,
              fontWeight: FontWeight.w700,
            ),
          ),
          const SizedBox(height: 8),
          if (summary.isNotEmpty)
            Text(
              summary,
              style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                fontFamily: detailFontFamily,
                fontWeight: FontWeight.w500,
                color: AppColors.gray2,
                height: 1.45,
              ),
            )
          else
            Text(
              '요약이 없습니다.',
              style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                fontFamily: detailFontFamily,
                fontWeight: FontWeight.w500,
                color: Colors.white70,
              ),
            ),
          if (keywords.isNotEmpty) ...[
            const SizedBox(height: 10),
            Wrap(
              spacing: 8,
              runSpacing: 8,
              children: keywords
                  .map(
                    (k) => Container(
                      padding: const EdgeInsets.symmetric(
                        horizontal: 10,
                        vertical: 6,
                      ),
                      decoration: BoxDecoration(
                        color: AppColors.darkGray,
                        borderRadius: BorderRadius.circular(12),
                      ),
                      child: Text(
                        '#${k.name}',
                        style: Theme.of(context).textTheme.bodySmall?.copyWith(
                          fontFamily: detailFontFamily,
                          color: AppColors.gray4,
                          fontWeight: FontWeight.w500,
                        ),
                      ),
                    ),
                  )
                  .toList(),
            ),
          ],
        ],
      ),
    );
  }
}
