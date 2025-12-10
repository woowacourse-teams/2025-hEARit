import 'package:flutter/material.dart';
import 'package:hearit/core/theme/app_colors.dart';

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
    required this.bottomSpacing,
    this.centerStatusIcon,
    this.coverAssetPath = 'assets/images/explore_LP.png',
    this.isPlaying = false,
  });

  final ExploreFeedItem item;
  final String pitchLine;
  final Duration position;
  final VoidCallback onContinuePressed;
  final double horizontalPadding;
  final double bottomSpacing;
  final IconData? centerStatusIcon;
  final String coverAssetPath;
  final bool isPlaying;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final bool isTablet = MediaQuery.of(context).size.shortestSide >= 600;
    return Stack(
      children: [
        Column(
          children: [
            Padding(
              padding: EdgeInsets.only(
                left: horizontalPadding,
                right: horizontalPadding,
                top: 16,
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.center,
                children: [
                  ExploreHighlightBanner(text: pitchLine),
                  const SizedBox(height: 26),
                  Text(
                    item.title,
                    textAlign: TextAlign.center,
                    style: theme.textTheme.headlineSmall?.copyWith(
                      color: AppColors.gray4,
                      fontWeight: FontWeight.w800,
                      fontSize: 20,
                    ),
                  ),
                  const SizedBox(height: 10),
                  ExploreKeywords(keywords: item.keywords),
                ],
              ),
            ),
            Expanded(
              child: LayoutBuilder(
                builder: (context, constraints) {
                  final double side = constraints.biggest.shortestSide;
                  return Center(
                    child: SizedBox(
                      width: side,
                      height: side,
                      child: ExploreCover(
                        categoryColor: item.categoryColor,
                        assetPath: coverAssetPath,
                        isPlaying: isPlaying,
                        containerSide: side,
                      ),
                    ),
                  );
                },
              ),
            ),
            Padding(
              padding: EdgeInsets.fromLTRB(
                horizontalPadding,
                0,
                horizontalPadding,
                bottomSpacing,
              ),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  SizedBox(
                    height: isTablet
                        ? ScriptView.tabletPreferredHeight
                        : ScriptView.preferredHeight,
                    width: double.infinity,
                    child: ScriptView(
                      scripts: item.scripts ?? const [],
                      position: position,
                      isTablet: isTablet,
                    ),
                  ),
                  const SizedBox(height: 8),
                  Transform.translate(
                    offset: const Offset(0, 6),
                    child: ExploreContinueButton(onPressed: onContinuePressed),
                  ),
                ],
              ),
            ),
          ],
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
              child: Icon(icon, size: 40, color: AppColors.gray4),
            ),
          ),
        ),
      ),
    );
  }
}
