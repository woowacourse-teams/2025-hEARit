import 'package:flutter/material.dart';
import 'package:hearit/core/theme/app_colors.dart';

import '../detail_font.dart';
import '../hearit_detail.dart';

class HearitInfoCard extends StatelessWidget {
  const HearitInfoCard({
    super.key,
    required this.detail,
    required this.formatDate,
    this.isTablet = false,
  });

  final HearitDetail detail;
  final String Function(DateTime date) formatDate;
  final bool isTablet;

  @override
  Widget build(BuildContext context) {
    final double titleSize = isTablet ? 23 : 18;
    final double dateSize = isTablet ? 18 : 14;
    final double maxCardWidth = isTablet ? 500 : double.infinity;

    Widget card = Container(
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
        mainAxisAlignment: MainAxisAlignment.end,
        children: [
          LayoutBuilder(
            builder: (context, constraints) {
              final double baseSize = constraints.maxWidth.clamp(0.0, 240.0);
              final double imageSize = isTablet ? baseSize * 1.5 : baseSize;
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
          const SizedBox(height: 12),
          Padding(
            padding: const EdgeInsets.only(bottom: 4),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              mainAxisSize: MainAxisSize.min,
              children: [
                Text(
                  detail.title,
                  style: Theme.of(context).textTheme.titleMedium?.copyWith(
                    fontFamily: detailFontFamily,
                    color: AppColors.gray4,
                    fontSize: titleSize,
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
                    fontSize: dateSize,
                    fontWeight: FontWeight.w500,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );

    if (isTablet) {
      card = Center(
        child: ConstrainedBox(
          constraints: BoxConstraints(maxWidth: maxCardWidth),
          child: AspectRatio(aspectRatio: 1, child: card),
        ),
      );
    }

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 8),
      child: card,
    );
  }
}
