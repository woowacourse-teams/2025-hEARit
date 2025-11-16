import 'package:flutter/material.dart';

class ExploreHighlightBanner extends StatelessWidget {
  const ExploreHighlightBanner({super.key, required this.text});

  final String text;

  @override
  Widget build(BuildContext context) {
    const highlightPhrase = '1분 미리듣기';
    final parts = text.split(highlightPhrase);
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.symmetric(horizontal: 18, vertical: 14),
      decoration: BoxDecoration(
        color: Colors.white.withOpacity(0.04),
        borderRadius: BorderRadius.circular(12),
      ),
      child: RichText(
        textAlign: TextAlign.center,
        text: TextSpan(
          style: Theme.of(context).textTheme.bodyMedium?.copyWith(
            color: Colors.white70,
            fontSize: 15,
            height: 1.4,
          ),
          children: [
            TextSpan(text: parts.first),
            TextSpan(
              text: highlightPhrase,
              style: const TextStyle(color: Color(0xFFA86BFF)),
            ),
            if (parts.length > 1)
              TextSpan(
                text: parts.sublist(1).join(highlightPhrase),
                style: TextStyle(color: Colors.white.withOpacity(0.85)),
              ),
          ],
        ),
      ),
    );
  }
}

class ExploreKeywords extends StatelessWidget {
  const ExploreKeywords({super.key, required this.keywords});

  final List<String> keywords;

  @override
  Widget build(BuildContext context) {
    return Wrap(
      spacing: 6,
      runSpacing: 6,
      children: keywords
          .map(
            (keyword) => Text(
              '#$keyword',
              style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                color: Colors.white70,
                fontSize: 14,
                fontWeight: FontWeight.w500,
              ),
            ),
          )
          .toList(),
    );
  }
}

class ExploreCover extends StatelessWidget {
  const ExploreCover({
    super.key,
    required this.categoryColor,
    required this.assetPath,
  });

  final Color categoryColor;
  final String assetPath;

  @override
  Widget build(BuildContext context) {
    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        SizedBox(
          height: 400,
          width: 400,
          child: Stack(
            clipBehavior: Clip.none,
            alignment: Alignment.center,
            children: [
              Align(
                alignment: Alignment.center,
                child: Transform.translate(
                  offset: const Offset(0, 0),
                  child: Container(
                    width: 100,
                    height: 100,
                    decoration: BoxDecoration(
                      color: categoryColor,
                      borderRadius: BorderRadius.circular(12),
                    ),
                  ),
                ),
              ),
              Image.asset(
                assetPath,
                height: 400,
                width: 400,
                fit: BoxFit.contain,
              ),
            ],
          ),
        ),
      ],
    );
  }
}

class ExploreContinueButton extends StatelessWidget {
  const ExploreContinueButton({
    super.key,
    required this.onPressed,
    this.label = '팟캐스트 이어듣기',
  });

  final VoidCallback onPressed;
  final String label;

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      width: double.infinity,
      child: ElevatedButton(
        style: ElevatedButton.styleFrom(
          backgroundColor: const Color(0xFF3A3A3F),
          foregroundColor: Colors.white,
          elevation: 0,
          padding: const EdgeInsets.symmetric(vertical: 16, horizontal: 20),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(12),
          ),
        ),
        onPressed: onPressed,
        child: Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text(
              label,
              style: Theme.of(context).textTheme.titleMedium?.copyWith(
                color: Colors.white,
                fontWeight: FontWeight.w700,
                fontSize: 16,
              ),
            ),
            const Icon(Icons.chevron_right, color: Colors.white70, size: 35),
          ],
        ),
      ),
    );
  }
}
