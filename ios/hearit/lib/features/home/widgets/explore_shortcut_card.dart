import 'package:flutter/material.dart';
import 'package:hearit/core/theme/app_colors.dart';

class ExploreShortcutCard extends StatelessWidget {
  const ExploreShortcutCard({super.key, this.onTap});

  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    return Material(
      color: Colors.transparent,
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(16),
        child: Ink(
          decoration: BoxDecoration(
            gradient: const LinearGradient(
              colors: [Color(0xFF24A8FF), Color(0xFF873BFF)],
              begin: Alignment.centerLeft,
              end: Alignment.centerRight,
            ),
            borderRadius: BorderRadius.circular(8),
            boxShadow: [
              BoxShadow(
                color: Colors.black.withOpacity(0.35),
                blurRadius: 14,
                offset: const Offset(0, 8),
              ),
            ],
          ),
          padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 18),
          child: Row(
            children: [
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Text(
                      '숏캐스트를 구경하고',
                      style: Theme.of(context).textTheme.titleMedium?.copyWith(
                        color: AppColors.gray4,
                        fontWeight: FontWeight.bold,
                        fontSize: 18,
                      ),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      '더 많은 팟캐스트를 북마크해보세요!',
                      style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                        color: AppColors.gray4.withOpacity(0.9),
                        fontSize: 16,
                        height: 1.2,
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(width: 12),
              Container(
                padding: const EdgeInsets.all(8),
                decoration: BoxDecoration(
                  color: AppColors.gray4.withOpacity(0.15),
                  shape: BoxShape.circle,
                ),
                child: const Icon(
                  Icons.arrow_forward_rounded,
                  color: AppColors.gray4,
                  size: 28,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
