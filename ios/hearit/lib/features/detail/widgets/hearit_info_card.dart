import 'package:flutter/material.dart';
import 'package:hearit/core/theme/app_colors.dart';

import '../detail_font.dart';
import '../hearit_detail.dart';

class HearitInfoCard extends StatelessWidget {
  const HearitInfoCard({
    super.key,
    required this.detail,
    required this.formatDate,
  });

  final HearitDetail detail;
  final String Function(DateTime date) formatDate;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 8),
      child: Container(
        width: double.infinity,
        padding: const EdgeInsets.fromLTRB(22, 4, 22, 8),
        decoration: BoxDecoration(
          color: detail.accentColor,
          borderRadius: BorderRadius.circular(8),
          boxShadow: [
            BoxShadow(
              color: Colors.black.withOpacity(0.35),
              blurRadius: 22,
              offset: const Offset(0, 16),
            ),
          ],
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            LayoutBuilder(
              builder: (context, constraints) {
                final double imageSize = constraints.maxWidth.clamp(0.0, 240.0);
                return Center(
                  child: Padding(
                    padding: const EdgeInsets.only(left: 15),
                    child: Image.asset(
                      'assets/images/detail_LP.png',
                      width: imageSize,
                      height: imageSize,
                      fit: BoxFit.contain,
                      errorBuilder: (context, error, stackTrace) {
                        return const Icon(
                          Icons.music_note_rounded,
                          color: Colors.white70,
                          size: 120,
                        );
                      },
                    ),
                  ),
                );
              },
            ),
            Text(
              detail.title,
              style: Theme.of(context).textTheme.titleMedium?.copyWith(
                fontFamily: detailFontFamily,
                color: AppColors.gray4,
                fontSize: 18,
                fontWeight: FontWeight.bold,
                height: 1.35,
              ),
            ),
            const SizedBox(height: 3),
            Text(
              formatDate(detail.createdAt),
              style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                fontFamily: detailFontFamily,
                color: AppColors.gray4,
                fontSize: 14,
                fontWeight: FontWeight.w500,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
