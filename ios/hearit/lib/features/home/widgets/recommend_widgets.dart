import 'package:flutter/material.dart';

import '../home_models.dart';

class RecommendCard extends StatelessWidget {
  const RecommendCard({super.key, required this.data, required this.onTap});

  final RecommendCardData data;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        decoration: BoxDecoration(
          color: data.categoryColor,
          borderRadius: BorderRadius.circular(10),
          boxShadow: [
            BoxShadow(
              color: Colors.black.withOpacity(0.3),
              blurRadius: 12,
              offset: const Offset(0, 6),
            ),
          ],
        ),
        padding: const EdgeInsets.fromLTRB(20, 12, 20, 20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              data.categoryName,
              style: Theme.of(context).textTheme.titleMedium?.copyWith(
                    color: Colors.white,
                    fontWeight: FontWeight.bold,
                    fontSize: 22,
                  ),
            ),
            const SizedBox(height: 12),
            Expanded(
              child: Align(
                alignment: Alignment.center,
                child: Image.asset(
                  'assets/images/LP.png',
                  width: 200,
                  height: 200,
                  fit: BoxFit.contain,
                  errorBuilder: (context, error, stackTrace) {
                    return const Icon(
                      Icons.music_note,
                      color: Colors.white54,
                      size: 100,
                    );
                  },
                ),
              ),
            ),
            const SizedBox(height: 16),
            Text(
              data.title,
              style: Theme.of(context).textTheme.titleMedium?.copyWith(
                    color: Colors.white,
                    fontWeight: FontWeight.bold,
                    fontSize: 18,
                  ),
              maxLines: 2,
              overflow: TextOverflow.ellipsis,
            ),
          ],
        ),
      ),
    );
  }
}

class RecommendIndicator extends StatelessWidget {
  const RecommendIndicator({
    super.key,
    required this.currentPage,
    required this.itemCount,
  });

  final double currentPage;
  final int itemCount;

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      crossAxisAlignment: CrossAxisAlignment.center,
      children: List.generate(itemCount, (index) {
        final double distance = (currentPage - index).abs();
        final bool isActive = distance < 0.5;

        final double width = isActive ? 11.0 : 9.0;
        final Color color = isActive
            ? const Color(0xFFA86BFF)
            : const Color(0xFFB2B4B6);

        return AnimatedContainer(
          duration: const Duration(milliseconds: 200),
          margin: const EdgeInsets.symmetric(horizontal: 8),
          width: width,
          height: width,
          decoration: BoxDecoration(color: color, shape: BoxShape.circle),
        );
      }),
    );
  }
}
