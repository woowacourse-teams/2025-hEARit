import 'package:flutter/material.dart';
import 'package:hearit/core/theme/app_colors.dart';

import '../search_models.dart';

class SearchResultCard extends StatelessWidget {
  const SearchResultCard({super.key, required this.data, this.onTap});

  final SearchHearit data;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final bool isTablet = MediaQuery.of(context).size.shortestSide >= 600;
    final double fontDelta = isTablet ? 5 : 0;
    final durationText = data.formattedPlayTime;
    final keywords = data.keywords
        .where((k) => k.isNotEmpty)
        .take(3)
        .map((k) => '# $k')
        .toList();
    final progress = data.progress;
    final double titleHeight = isTablet ? 64 : 52;

    return Material(
      color: Colors.transparent,
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(8),
        child: Ink(
          decoration: BoxDecoration(
            color: AppColors.gray1,
            borderRadius: BorderRadius.circular(12),
          ),
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              SizedBox(
                height: titleHeight, // reserve space for up to 2 lines of title
                child: Row(
                  children: [
                    Expanded(
                      child: Text(
                        data.title,
                        style: theme.textTheme.titleMedium?.copyWith(
                          color: AppColors.gray4,
                          fontWeight: FontWeight.w700,
                          fontSize:
                              (theme.textTheme.titleMedium?.fontSize ?? 16) +
                              fontDelta,
                        ),
                        maxLines: 2,
                        overflow: TextOverflow.ellipsis,
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 8),
              Row(
                children: [
                  Expanded(
                    child: Wrap(
                      spacing: 8,
                      runSpacing: 4,
                      children: keywords
                          .map(
                            (keyword) => Text(
                              keyword,
                              style: theme.textTheme.bodySmall?.copyWith(
                                color: Colors.white70,
                                fontWeight: FontWeight.w600,
                                fontSize:
                                    (theme.textTheme.bodySmall?.fontSize ??
                                        12) +
                                    fontDelta,
                              ),
                            ),
                          )
                          .toList(),
                    ),
                  ),
                  const SizedBox(width: 12),
                  Text(
                    durationText,
                    style: theme.textTheme.bodySmall?.copyWith(
                      color: Colors.white70,
                      fontWeight: FontWeight.w600,
                      fontSize:
                          (theme.textTheme.bodySmall?.fontSize ?? 12) +
                          fontDelta,
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 6),
              _SearchProgressBar(progress: progress),
            ],
          ),
        ),
      ),
    );
  }
}

class _SearchProgressBar extends StatelessWidget {
  const _SearchProgressBar({required this.progress});

  final double? progress;

  @override
  Widget build(BuildContext context) {
    final double safeProgress = (progress ?? 0.0).clamp(0, 1).toDouble();
    return LayoutBuilder(
      builder: (context, constraints) {
        final double barWidth = constraints.maxWidth * safeProgress;
        return Container(
          height: 4,
          decoration: BoxDecoration(
            color: AppColors.darkGray,
            borderRadius: BorderRadius.circular(999),
          ),
          child: Align(
            alignment: Alignment.centerLeft,
            child: Container(
              width: barWidth,
              decoration: BoxDecoration(
                color: AppColors.hearitPurple2,
                borderRadius: BorderRadius.circular(999),
              ),
            ),
          ),
        );
      },
    );
  }
}
