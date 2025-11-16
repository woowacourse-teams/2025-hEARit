import 'package:flutter/material.dart';

import '../../../core/presentation/widgets/script_view.dart';
import '../explore_models.dart';
import 'explore_widgets.dart';

class ExploreFeedPage extends StatelessWidget {
  const ExploreFeedPage({
    super.key,
    required this.item,
    required this.pitchLine,
    required this.position,
    required this.onContinuePressed,
    required this.horizontalPadding,
    this.centerStatusIcon,
    this.coverAssetPath = 'assets/images/explore_LP.png',
  });

  final ExploreFeedItem item;
  final String pitchLine;
  final Duration position;
  final VoidCallback onContinuePressed;
  final double horizontalPadding;
  final IconData? centerStatusIcon;
  final String coverAssetPath;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Stack(
      children: [
        Positioned(
          top: 16,
          left: horizontalPadding,
          right: horizontalPadding,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.center,
            children: [
              ExploreHighlightBanner(text: pitchLine),
              const SizedBox(height: 26),
              Text(
                item.title,
                textAlign: TextAlign.center,
                style: theme.textTheme.headlineSmall?.copyWith(
                  color: Colors.white,
                  fontWeight: FontWeight.w800,
                  fontSize: 24,
                ),
              ),
              const SizedBox(height: 10),
              ExploreKeywords(keywords: item.keywords),
              const SizedBox(height: 8),
              Transform.translate(
                offset: const Offset(0, -30),
                child: ExploreCover(
                  categoryColor: item.categoryColor,
                  assetPath: coverAssetPath,
                ),
              ),
            ],
          ),
        ),
        Positioned(
          left: horizontalPadding,
          right: horizontalPadding,
          bottom: 0,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              SizedBox(
                height: 170,
                width: double.infinity,
                child: ScriptView(
                  scripts: item.scripts ?? const [],
                  position: position,
                ),
              ),
              const SizedBox(height: 8),
              ExploreContinueButton(onPressed: onContinuePressed),
            ],
          ),
        ),
        if (centerStatusIcon != null)
          CenterStatusOverlay(icon: centerStatusIcon!),
      ],
    );
  }
}

class CenterStatusOverlay extends StatelessWidget {
  const CenterStatusOverlay({super.key, required this.icon});

  final IconData icon;

  @override
  Widget build(BuildContext context) {
    return Positioned.fill(
      child: IgnorePointer(
        child: Transform.translate(
          offset: const Offset(0, 100),
          child: Center(
            child: Container(
              padding: const EdgeInsets.all(20),
              decoration: BoxDecoration(
                color: const Color(0xFF9533F5).withOpacity(0.7),
                shape: BoxShape.circle,
              ),
              child: Icon(
                icon,
                size: 40,
                color: Colors.white,
              ),
            ),
          ),
        ),
      ),
    );
  }
}
