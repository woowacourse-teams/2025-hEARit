import 'package:flutter/material.dart';
import 'package:hearit/core/theme/app_colors.dart';

import '../home_models.dart';
import '../home_font.dart';

class ListeningSection extends StatelessWidget {
  const ListeningSection({
    super.key,
    required this.title,
    required this.items,
    this.showChevron = false,
    required this.onTap,
  });

  final String title;
  final List<ListeningCardData> items;
  final bool showChevron;
  final void Function(ListeningCardData data) onTap;

  @override
  Widget build(BuildContext context) {
    if (items.isEmpty) {
      return Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 20),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  title,
                  style: Theme.of(context).textTheme.titleMedium?.copyWith(
                    fontFamily: homeTitleFontFamily,
                    fontWeight: FontWeight.bold,
                    color: AppColors.gray4,
                    fontSize: title.contains('북마크') ? 22 : 20,
                  ),
                ),
                if (showChevron)
                  Icon(
                    Icons.chevron_right,
                    color: AppColors.gray4.withOpacity(0.9),
                  ),
              ],
            ),
          ),
          const SizedBox(height: 10),
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 20),
            child: Text(
              '아직 준비된 항목이 없습니다.',
              style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                color: AppColors.gray4.withOpacity(0.7),
              ),
            ),
          ),
          const SizedBox(height: 16),
        ],
      );
    }

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 20),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                title,
                style: Theme.of(context).textTheme.titleMedium?.copyWith(
                  fontFamily: homeTitleFontFamily,
                  fontWeight: FontWeight.bold,
                  color: AppColors.gray4,
                  fontSize: title.contains('북마크') ? 22 : 20,
                ),
              ),
              if (showChevron)
                Icon(
                  Icons.chevron_right,
                  color: AppColors.gray4.withOpacity(0.9),
                ),
            ],
          ),
        ),
        const SizedBox(height: 18),
        SizedBox(
          height: 190,
          child: ListView.separated(
            scrollDirection: Axis.horizontal,
            padding: const EdgeInsets.only(left: 20),
            itemCount: items.length,
            separatorBuilder: (_, __) => const SizedBox(width: 20),
            itemBuilder: (context, index) {
              final data = items[index];
              return ListeningCard(data: data, onTap: () => onTap(data));
            },
          ),
        ),
      ],
    );
  }
}

class ListeningCard extends StatelessWidget {
  const ListeningCard({super.key, required this.data, required this.onTap});

  final ListeningCardData data;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: SizedBox(
        width: 150,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Container(
              height: 88,
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 12),
              decoration: BoxDecoration(
                color: data.backgroundColor,
                borderRadius: BorderRadius.circular(8),
                boxShadow: [
                  BoxShadow(
                    color: Colors.black.withOpacity(0.25),
                    blurRadius: 12,
                    offset: const Offset(0, 10),
                  ),
                ],
              ),
              child: Center(
                child: Text(
                  data.title,
                  textAlign: TextAlign.center,
                  style: Theme.of(context).textTheme.titleLarge?.copyWith(
                    color: AppColors.gray4,
                    fontWeight: FontWeight.w900,
                    fontSize: 22,
                    letterSpacing: 0.4,
                  ),
                ),
              ),
            ),
            const SizedBox(height: 14),
            SizedBox(
              height: 44.8,
              child: Text(
                data.description,
                style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                  color: AppColors.gray4,
                  fontSize: 16,
                  height: 1.4,
                  fontWeight: FontWeight.bold,
                ),
                maxLines: 2,
                overflow: TextOverflow.ellipsis,
              ),
            ),
            Padding(
              padding: const EdgeInsets.only(top: 6),
              child: ListeningProgressBar(progress: data.progress ?? 0.0),
            ),
          ],
        ),
      ),
    );
  }
}

class ListeningProgressBar extends StatelessWidget {
  const ListeningProgressBar({super.key, required this.progress});

  final double progress;

  @override
  Widget build(BuildContext context) {
    final double safeProgress = progress.clamp(0, 1).toDouble();
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
