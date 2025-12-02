import 'package:flutter/material.dart';

import '../search_models.dart';

class SearchCategoryGrid extends StatelessWidget {
  const SearchCategoryGrid({super.key, required this.categories, this.onTap});

  final List<SearchCategory> categories;
  final void Function(SearchCategory category)? onTap;

  @override
  Widget build(BuildContext context) {
    if (categories.isEmpty) return const SizedBox.shrink();
    return GridView.builder(
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 2,
        mainAxisSpacing: 16,
        crossAxisSpacing: 16,
        childAspectRatio: 2.8,
      ),
      itemCount: categories.length,
      itemBuilder: (context, index) {
        final category = categories[index];
        return GestureDetector(
          onTap: onTap == null ? null : () => onTap!(category),
          child: Container(
            decoration: BoxDecoration(
              color: category.color,
              borderRadius: BorderRadius.circular(8),
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withOpacity(0.12),
                  blurRadius: 8,
                  offset: const Offset(0, 6),
                ),
              ],
            ),
            child: Center(
              child: Text(
                category.name,
                style: Theme.of(context).textTheme.titleSmall?.copyWith(
                  color: Colors.white,
                  fontWeight: FontWeight.w600,
                  letterSpacing: 0.4,
                  fontSize: 18,
                ),
              ),
            ),
          ),
        );
      },
    );
  }
}
