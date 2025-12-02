import 'package:flutter/material.dart';
import 'dart:ui';
import 'package:hearit/core/theme/app_colors.dart';

import 'package:hearit/features/detail/detail_font.dart';
import 'package:hearit/features/detail/hearit_detail_viewmodel.dart';

class ScriptView extends StatefulWidget {
  const ScriptView({super.key, required this.scripts, required this.position});

  final List<ScriptLine> scripts;
  final Duration position;

  @override
  State<ScriptView> createState() => _ScriptViewState();
}

class _ScriptViewState extends State<ScriptView>
    with SingleTickerProviderStateMixin {
  late int _currentIndex;
  late final AnimationController _controller;
  bool _isAnimating = false;

  static const double _lineHeight = 44;
  static const double _lineSpacing = 10;

  double get _slotExtent => _lineHeight + _lineSpacing;
  double get _containerHeight => _lineHeight * 3 + _lineSpacing * 2;

  @override
  void initState() {
    super.initState();
    _currentIndex = _findCurrentIndex(widget.position);
    _controller = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 650),
    );

    _controller.addStatusListener((status) {
      if (status == AnimationStatus.completed) {
        setState(() {
          _isAnimating = false;
          if (_currentIndex < widget.scripts.length - 1) {
            _currentIndex += 1;
          }
          _controller.reset();
        });
      }
    });
  }

  @override
  void didUpdateWidget(covariant ScriptView oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (widget.scripts.isEmpty) return;
    final nextIndex = _findCurrentIndex(widget.position);

    if (_isAnimating) {
      // If user seeks far or backwards during animation, snap to target.
      if (nextIndex <= _currentIndex || nextIndex - _currentIndex > 1) {
        _controller.stop();
        setState(() {
          _isAnimating = false;
          _currentIndex = nextIndex;
          _controller.reset();
        });
      }
      return;
    }

    if (nextIndex == _currentIndex + 1) {
      _startForwardAnimation();
    } else if (nextIndex != _currentIndex) {
      setState(() {
        _currentIndex = nextIndex;
      });
    }
  }

  void _startForwardAnimation() {
    if (_currentIndex >= widget.scripts.length - 1) return;
    setState(() {
      _isAnimating = true;
    });
    _controller.forward(from: 0);
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    if (widget.scripts.isEmpty) {
      return const SizedBox.shrink();
    }

    return ClipRect(
      child: SizedBox(
        // Match the exact block height to avoid extra top/bottom space during slide
        height: _containerHeight,
        width: double.infinity,
        child: AnimatedBuilder(
          animation: _controller,
          builder: (context, _) {
            final progress =
                _isAnimating ? Curves.easeOutCubic.transform(_controller.value) : 0.0;
            final highlightBlend = _highlightBlend(progress);

            return Stack(
              children: [
                _buildLine(
                  index: _currentIndex - 1,
                  baseTop: 0,
                  emphasis: _lineEmphasis(_currentIndex - 1, highlightBlend),
                  progress: progress,
                ),
                _buildLine(
                  index: _currentIndex,
                  baseTop: _slotExtent,
                  emphasis: _lineEmphasis(_currentIndex, highlightBlend),
                  progress: progress,
                ),
                _buildLine(
                  index: _currentIndex + 1,
                  baseTop: _slotExtent * 2,
                  emphasis: _lineEmphasis(_currentIndex + 1, highlightBlend),
                  progress: progress,
                ),
                if (_currentIndex + 2 < widget.scripts.length || _isAnimating)
                  _buildLine(
                    index: _currentIndex + 2,
                    baseTop: _slotExtent * 3,
                    emphasis: _lineEmphasis(_currentIndex + 2, highlightBlend),
                    progress: progress,
                  ),
              ],
            );
          },
        ),
      ),
    );
  }

  Widget _buildLine({
    required int index,
    required double baseTop,
    required double emphasis,
    required double progress,
  }) {
    final text = _textAt(index);
    final dy = baseTop - (progress * _slotExtent);

    return Positioned(
      top: dy,
      left: 0,
      right: 0,
      child: _ScriptLine(
        text: text,
        emphasis: emphasis,
      ),
    );
  }

  double _highlightBlend(double progress) {
    if (!_isAnimating) return 0.0;
    const double start = 0.35;
    const double end = 0.65;
    final t = ((progress - start) / (end - start)).clamp(0.0, 1.0);
    return Curves.easeInOut.transform(t);
  }

  double _lineEmphasis(int index, double blend) {
    if (!_isAnimating) return index == _currentIndex ? 1.0 : 0.0;
    final nextIndex = _currentIndex + 1;
    if (index == _currentIndex) return 1.0 - blend;
    if (index == nextIndex) return (_currentIndex + 1 < widget.scripts.length) ? blend : 0.0;
    return 0.0;
  }

  String _textAt(int index) {
    if (index < 0 || index >= widget.scripts.length) return '';
    return widget.scripts[index].text;
  }

  int _findCurrentIndex(Duration position) {
    if (widget.scripts.isEmpty) return 0;

    if (position < widget.scripts.first.start) return 0;

    for (int i = 0; i < widget.scripts.length; i++) {
      final line = widget.scripts[i];
      if (position >= line.start && position < line.end) {
        return i;
      }
    }
    return widget.scripts.length - 1;
  }
}

class _ScriptLine extends StatelessWidget {
  const _ScriptLine({required this.text, required this.emphasis});

  final String text;
  final double emphasis;

  @override
  Widget build(BuildContext context) {
    const double lineBoxHeight = 44;
    final clamped = emphasis.clamp(0.0, 1.0);
    final Color baseColor =
        text.isEmpty ? Colors.transparent : Color.lerp(AppColors.gray2, AppColors.gray4, clamped)!;
    final double fontSize = lerpDouble(14, 15, clamped)!;

    return SizedBox(
      height: lineBoxHeight,
      child: Center(
        child: Text(
          text.isEmpty ? '\u00A0' : text,
          textAlign: TextAlign.center,
          maxLines: 2,
          overflow: TextOverflow.ellipsis,
          style: Theme.of(context).textTheme.bodyMedium?.copyWith(
            fontFamily: detailFontFamily,
            color: baseColor,
            fontWeight: FontWeight.w500,
            fontSize: fontSize,
            height: 1.4,
          ),
        ),
      ),
    );
  }
}
