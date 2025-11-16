import 'package:flutter/material.dart';

class DetailHeader extends StatelessWidget {
  const DetailHeader({
    super.key,
    required this.categoryName,
    required this.onBack,
    required this.onShare,
  });

  final String categoryName;
  final VoidCallback onBack;
  final VoidCallback onShare;

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        IconButton(
          padding: EdgeInsets.zero,
          constraints: const BoxConstraints(minWidth: 32, minHeight: 32),
          icon: const Icon(Icons.arrow_back_ios_new_rounded, size: 24),
          color: Colors.white,
          onPressed: onBack,
        ),
        Expanded(
          child: Text(
            categoryName,
            textAlign: TextAlign.center,
            style: Theme.of(context).textTheme.titleMedium?.copyWith(
              color: Colors.white,
              fontWeight: FontWeight.bold,
              fontSize: 20,
            ),
          ),
        ),
        IconButton(
          padding: EdgeInsets.zero,
          constraints: const BoxConstraints(minWidth: 32, minHeight: 32),
          icon: const Icon(Icons.share, size: 22),
          color: Colors.white,
          onPressed: onShare,
        ),
      ],
    );
  }
}
