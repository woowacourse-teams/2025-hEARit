import 'package:flutter/material.dart';

import '../search_models.dart';

class SearchResultCard extends StatelessWidget {
  const SearchResultCard({super.key, required this.data, this.onTap});

  final SearchHearit data;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final durationText = data.formattedPlayTime;
    final keywords = data.keywords
        .where((k) => k.isNotEmpty)
        .take(3)
        .map((k) => '# $k')
        .toList();
    final progress = data.progress;

    return Material(
      color: Colors.transparent,
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(8),
        child: Ink(
          decoration: BoxDecoration(
            color: const Color(0xFF2A2A2A),
            borderRadius: BorderRadius.circular(12),
          ),
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              SizedBox(
                height: 52, // reserve space for up to 2 lines of title
                child: Row(
                  children: [
                    Expanded(
                      child: Text(
                        data.title,
                        style: theme.textTheme.titleMedium?.copyWith(
                          color: Colors.white,
                          fontWeight: FontWeight.w700,
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
    if (progress == null) {
      return Container(
        height: 6,
        decoration: BoxDecoration(
          color: const Color(0xFF3B3B46),
          borderRadius: BorderRadius.circular(999),
        ),
      );
    }
    final double clamped = progress!.clamp(0, 1).toDouble();
    return Container(
      height: 6,
      decoration: BoxDecoration(
        color: const Color(0xFF3B3B46),
        borderRadius: BorderRadius.circular(999),
      ),
      child: Align(
        alignment: Alignment.centerLeft,
        child: FractionallySizedBox(
          widthFactor: clamped == 0 ? 0.02 : clamped,
          child: Container(
            decoration: BoxDecoration(
              color: const Color(0xFFA86BFF),
              borderRadius: BorderRadius.circular(999),
            ),
          ),
        ),
      ),
    );
  }
}
