import 'package:flutter/material.dart';

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
  int? _incomingIndex;
  late final AnimationController _controller;
  late final Animation<Offset> _currentOffset;
  late final Animation<Offset> _incomingOffset;

  @override
  void initState() {
    super.initState();
    _currentIndex = _findCurrentIndex(widget.position);
    _controller = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 320),
    );
    _currentOffset = Tween<Offset>(
      begin: Offset.zero,
      end: const Offset(0, -1),
    ).chain(CurveTween(curve: Curves.fastOutSlowIn)).animate(_controller);
    _incomingOffset = Tween<Offset>(
      begin: const Offset(0, 1),
      end: Offset.zero,
    ).chain(CurveTween(curve: Curves.fastOutSlowIn)).animate(_controller);

    _controller.addStatusListener((status) {
      if (status == AnimationStatus.completed && _incomingIndex != null) {
        setState(() {
          _currentIndex = _incomingIndex!;
          _incomingIndex = null;
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
    if (nextIndex != _currentIndex && !_controller.isAnimating) {
      setState(() {
        _incomingIndex = nextIndex;
      });
      _controller.forward();
    } else if (nextIndex == _currentIndex && _incomingIndex != null) {
      _controller.reset();
      setState(() {
        _incomingIndex = null;
      });
    }
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
        height: 160,
        width: double.infinity,
        child: Stack(
          alignment: Alignment.center,
          children: [
            SlideTransition(
              position: _currentOffset,
              child: _ScriptBlock(
                key: ValueKey<int>(_currentIndex),
                scripts: widget.scripts,
                index: _currentIndex,
              ),
            ),
            if (_incomingIndex != null)
              SlideTransition(
                position: _incomingOffset,
                child: _ScriptBlock(
                  key: ValueKey<int>(_incomingIndex!),
                  scripts: widget.scripts,
                  index: _incomingIndex!,
                ),
              ),
          ],
        ),
      ),
    );
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

class _ScriptBlock extends StatelessWidget {
  const _ScriptBlock({super.key, required this.scripts, required this.index});

  final List<ScriptLine> scripts;
  final int index;

  @override
  Widget build(BuildContext context) {
    final prevText = index > 0 ? scripts[index - 1].text : '';
    final currentText = scripts[index].text;
    final nextText = index < scripts.length - 1 ? scripts[index + 1].text : '';

    return Column(
      mainAxisAlignment: MainAxisAlignment.center,
      crossAxisAlignment: CrossAxisAlignment.center,
      children: [
        _ScriptLine(text: prevText, isHighlight: false),
        const SizedBox(height: 10),
        _ScriptLine(text: currentText, isHighlight: true),
        const SizedBox(height: 10),
        _ScriptLine(text: nextText, isHighlight: false),
      ],
    );
  }
}

class _ScriptLine extends StatelessWidget {
  const _ScriptLine({required this.text, required this.isHighlight});

  final String text;
  final bool isHighlight;

  @override
  Widget build(BuildContext context) {
    const double lineBoxHeight = 44;
    return SizedBox(
      height: lineBoxHeight,
      child: Center(
        child: Text(
          text.isEmpty ? '\u00A0' : text,
          textAlign: TextAlign.center,
          maxLines: 2,
          overflow: TextOverflow.ellipsis,
          style: Theme.of(context).textTheme.bodyMedium?.copyWith(
            color: isHighlight
                ? Colors.white
                : (text.isEmpty ? Colors.transparent : Colors.white70),
            fontWeight: isHighlight ? FontWeight.w700 : FontWeight.w500,
            fontSize: isHighlight ? 16 : 15,
            height: 1.4,
          ),
        ),
      ),
    );
  }
}
