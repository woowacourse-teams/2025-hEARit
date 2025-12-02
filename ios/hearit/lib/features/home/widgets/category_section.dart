import 'package:flutter/material.dart';
import 'package:hearit/core/theme/app_colors.dart';

import '../home_models.dart';

class CategorySection extends StatelessWidget {
  const CategorySection({
    super.key,
    required this.sections,
    required this.onHearitTap,
    this.onCategoryTap,
  });

  final List<CategorySectionData> sections;
  final void Function(CategorySectionData section, CategoryPodcastData podcast)
      onHearitTap;
  final void Function(CategorySectionData section)? onCategoryTap;

  @override
  Widget build(BuildContext context) {
    if (sections.isEmpty) {
      return Text(
        '아직 추천 카테고리가 없습니다.',
        style: Theme.of(context).textTheme.bodyMedium?.copyWith(
          color: AppColors.gray4.withOpacity(0.7),
        ),
      );
    }

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: List.generate(sections.length, (index) {
        final data = sections[index];
        return Padding(
          padding: EdgeInsets.only(
            bottom: index == sections.length - 1 ? 0 : 26,
          ),
          child: CategoryBlock(
            data: data,
            onHearitTap: onHearitTap,
            onCategoryTap: onCategoryTap,
          ),
        );
      }),
    );
  }
}

class CategoryBlock extends StatelessWidget {
  const CategoryBlock({
    super.key,
    required this.data,
    required this.onHearitTap,
    required this.onCategoryTap,
  });

  final CategorySectionData data;
  final void Function(CategorySectionData section, CategoryPodcastData podcast)
      onHearitTap;
  final void Function(CategorySectionData section)? onCategoryTap;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          children: [
            Flexible(
              child: GestureDetector(
                behavior: HitTestBehavior.translucent,
                onTap: () => onCategoryTap?.call(data),
                child: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Flexible(
                      child: Text(
                        '${data.categoryName} 카테고리',
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                        style: Theme.of(context).textTheme.titleMedium?.copyWith(
                              color: AppColors.gray4,
                              fontSize: 21,
                              fontWeight: FontWeight.bold,
                            ),
                      ),
                    ),
                    const SizedBox(width: 6),
                    Icon(Icons.chevron_right, color: AppColors.gray4, size: 28),
                  ],
                ),
              ),
            ),
          ],
        ),
        const SizedBox(height: 12),
        SizedBox(
          height: 142,
          child: ListView.separated(
            scrollDirection: Axis.horizontal,
            itemCount: data.podcasts.length,
            separatorBuilder: (_, __) => const SizedBox(width: 14),
            itemBuilder: (context, index) {
              final podcast = data.podcasts[index];
              return CategoryPodcastCard(
                podcast: podcast,
                accentColor: data.accentColor,
                onTap: () => onHearitTap(data, podcast),
              );
            },
          ),
        ),
      ],
    );
  }
}

class CategoryPodcastCard extends StatelessWidget {
  const CategoryPodcastCard({
    super.key,
    required this.podcast,
    required this.accentColor,
    required this.onTap,
  });

  final CategoryPodcastData podcast;
  final Color accentColor;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: SizedBox(
        width: 210,
        child: Container(
          padding: const EdgeInsets.all(18),
          decoration: BoxDecoration(
            color: AppColors.gray1,
            borderRadius: BorderRadius.circular(8),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Expanded(
                child: Text(
                  podcast.title,
                  style: Theme.of(context).textTheme.titleMedium?.copyWith(
                    color: AppColors.gray4,
                    fontSize: 16,
                    fontWeight: FontWeight.bold,
                    height: 1.3,
                  ),
                ),
              ),
              const SizedBox(height: 18),
              LayoutBuilder(
                builder: (context, constraints) {
                  return SizedBox(
                    width: constraints.maxWidth,
                    height: 18,
                    child: Stack(
                      clipBehavior: Clip.none,
                      children: [
                        Positioned(
                          left: -18,
                          right: -18,
                          child: Row(
                            children: [
                              Container(
                                width: 12,
                                height: 18,
                                decoration: BoxDecoration(
                                  color: accentColor,
                                  borderRadius: BorderRadius.circular(3),
                                ),
                              ),
                              const SizedBox(width: 10),
                              Expanded(
                                child: Container(
                                  height: 18,
                                  decoration: BoxDecoration(
                                    color: accentColor,
                                    borderRadius: BorderRadius.circular(3),
                                  ),
                                ),
                              ),
                            ],
                          ),
                        ),
                      ],
                    ),
                  );
                },
              ),
            ],
          ),
        ),
      ),
    );
  }
}
