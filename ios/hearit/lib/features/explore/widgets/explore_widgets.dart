import 'package:flutter/material.dart';
import 'package:hearit/core/theme/app_colors.dart';

class ExploreHighlightBanner extends StatelessWidget {
  const ExploreHighlightBanner({super.key, required this.text});

  final String text;

  @override
  Widget build(BuildContext context) {
    const highlightPhrase = '1분 미리듣기';
    final parts = text.split(highlightPhrase);
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.symmetric(horizontal: 18, vertical: 12),
      decoration: BoxDecoration(
        color: AppColors.gray1,
        borderRadius: BorderRadius.circular(8),
      ),
      child: RichText(
        textAlign: TextAlign.center,
        text: TextSpan(
          style: Theme.of(context).textTheme.bodyMedium?.copyWith(
            color: Colors.white70,
            fontSize: 13,
            height: 1.4,
          ),
          children: [
            TextSpan(text: parts.first),
            TextSpan(
              text: highlightPhrase,
              style: const TextStyle(color: AppColors.hearitPurple1),
            ),
            if (parts.length > 1)
              TextSpan(
                text: parts.sublist(1).join(highlightPhrase),
                style: TextStyle(color: AppColors.gray4),
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
                color: AppColors.gray2,
                fontSize: 14,
                fontWeight: FontWeight.w500,
              ),
            ),
          )
          .toList(),
    );
  }
}

class ExploreCover extends StatefulWidget {
  const ExploreCover({
    super.key,
    required this.categoryColor,
    required this.assetPath,
    this.isPlaying = false,
    this.containerSide,
  });

  final Color categoryColor;
  final String assetPath;
  final bool isPlaying;
  final double? containerSide;

  @override
  State<ExploreCover> createState() => _ExploreCoverState();
}

class _ExploreCoverState extends State<ExploreCover>
    with SingleTickerProviderStateMixin {
  late final AnimationController _controller = AnimationController(
    vsync: this,
    duration: const Duration(seconds: 6),
  );

  @override
  void initState() {
    super.initState();
    _syncSpin();
  }

  @override
  void didUpdateWidget(covariant ExploreCover oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.isPlaying != widget.isPlaying) {
      _syncSpin();
    }
  }

  void _syncSpin() {
    if (widget.isPlaying) {
      _controller.repeat();
    } else {
      _controller.stop();
    }
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final double containerSide = widget.containerSide ?? 300;
    final double lpSize = (containerSide).clamp(80.0, 500.0);
    final double boxSize = (containerSide * 0.33).clamp(30.0, lpSize * 0.3);

    return Stack(
      clipBehavior: Clip.none,
      alignment: Alignment.center,
      children: [
        Align(
          alignment: Alignment.center,
          child: Transform.translate(
            offset: const Offset(0, -10),
            child: Container(
              width: boxSize,
              height: boxSize,
              decoration: BoxDecoration(
                color: widget.categoryColor,
                borderRadius: BorderRadius.circular(12),
              ),
            ),
          ),
        ),
        Transform.translate(
          offset: const Offset(0, -10),
          child: RotationTransition(
            turns: _controller,
            child: Image.asset(
              widget.assetPath,
              height: lpSize,
              width: lpSize,
              fit: BoxFit.contain,
            ),
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
          backgroundColor: AppColors.gray1,
          foregroundColor: AppColors.gray4,
          elevation: 0,
          padding: const EdgeInsets.symmetric(vertical: 12, horizontal: 20),
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
        ),
        onPressed: onPressed,
        child: Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text(
              label,
              style: Theme.of(context).textTheme.titleMedium?.copyWith(
                color: AppColors.gray4,
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
