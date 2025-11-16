import 'package:flutter/material.dart';

import '../home_models.dart';

class CategorySection extends StatelessWidget {
  const CategorySection({
    super.key,
    required this.sections,
    required this.onHearitTap,
  });

  final List<CategorySectionData> sections;
  final void Function(CategorySectionData section, CategoryPodcastData podcast)
      onHearitTap;

  @override
  Widget build(BuildContext context) {
    if (sections.isEmpty) {
      return Text(
        '아직 추천 카테고리가 없습니다.',
        style: Theme.of(context).textTheme.bodyMedium?.copyWith(
              color: Colors.white70,
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
          child: CategoryBlock(data: data, onHearitTap: onHearitTap),
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
  });

  final CategorySectionData data;
  final void Function(CategorySectionData section, CategoryPodcastData podcast)
      onHearitTap;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          children: [
            Expanded(
              child: Text(
                '${data.categoryName} 카테고리',
                style: Theme.of(context).textTheme.titleMedium?.copyWith(
                      color: Colors.white,
                      fontSize: 21,
                      fontWeight: FontWeight.bold,
                    ),
              ),
            ),
            Icon(Icons.chevron_right, color: Colors.white.withOpacity(0.9)),
          ],
        ),
        const SizedBox(height: 14),
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
            color: const Color(0xFF2A2A33),
            borderRadius: BorderRadius.circular(14),
            boxShadow: [
              BoxShadow(
                color: Colors.black.withOpacity(0.25),
                blurRadius: 14,
                offset: const Offset(0, 10),
              ),
            ],
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Expanded(
                child: Text(
                  podcast.title,
                  style: Theme.of(context).textTheme.titleMedium?.copyWith(
                        color: Colors.white,
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
