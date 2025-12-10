import 'package:flutter/material.dart';

import '../detail_font.dart';

class DetailHeader extends StatelessWidget {
  const DetailHeader({
    super.key,
    required this.categoryName,
    required this.onBack,
    this.isTablet = false,
  });

  final String categoryName;
  final VoidCallback onBack;
  final bool isTablet;

  @override
  Widget build(BuildContext context) {
    const double sideSize = 40;
    final double titleSize = isTablet ? 25 : 20;
    return Row(
      children: [
        IconButton(
          padding: EdgeInsets.zero,
          constraints: const BoxConstraints.tightFor(
            width: sideSize,
            height: sideSize,
          ),
          icon: const Icon(Icons.arrow_back_ios_new_rounded, size: 24),
          color: Colors.white,
          onPressed: onBack,
        ),
        Expanded(
          child: Text(
            categoryName,
            textAlign: TextAlign.center,
            style: Theme.of(context).textTheme.titleMedium?.copyWith(
              fontFamily: detailFontFamily,
              color: Colors.white,
              fontWeight: FontWeight.bold,
              fontSize: titleSize,
            ),
          ),
        ),
        const SizedBox(width: sideSize, height: sideSize),
      ],
    );
  }
}
