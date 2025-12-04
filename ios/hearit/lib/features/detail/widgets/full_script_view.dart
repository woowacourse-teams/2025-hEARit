import 'package:flutter/material.dart';

import '../../../core/audio/hearit_player_controller.dart';
import '../../../core/theme/app_colors.dart';
import '../detail_font.dart';
import '../hearit_detail.dart';
import '../hearit_detail_viewmodel.dart';
import 'audio_controls.dart';

class FullScriptView extends StatefulWidget {
  const FullScriptView({
    super.key,
    required this.detail,
    required this.scripts,
    required this.controller,
    required this.viewModel,
  });

  final HearitDetail detail;
  final List<ScriptLine> scripts;
  final HearitPlayerController controller;
  final HearitDetailViewModel viewModel;

  @override
  State<FullScriptView> createState() => _FullScriptViewState();
}

class _FullScriptViewState extends State<FullScriptView> {
  @override
  Widget build(BuildContext context) {
    if (widget.scripts.isEmpty) {
      return const SizedBox.shrink();
    }

    final theme = Theme.of(context);
    return Material(
      color: AppColors.hearitBlack,
      child: SafeArea(
        top: true,
        bottom: false,
        child: Container(
          decoration: const BoxDecoration(
            color: AppColors.hearitBlack,
            borderRadius: BorderRadius.vertical(top: Radius.circular(18)),
          ),
          padding: const EdgeInsets.fromLTRB(
            24,
            14,
            24,
            0,
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.center,
            children: [
              Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Transform.translate(
                    offset: const Offset(0, -4),
                    child: IconButton(
                      padding: EdgeInsets.zero,
                      constraints: const BoxConstraints.tightFor(
                        width: 44,
                        height: 44,
                      ),
                      onPressed: () => Navigator.of(context).pop(),
                      icon: const Icon(
                        Icons.keyboard_arrow_down_rounded,
                        size: 34,
                        color: AppColors.gray4,
                      ),
                    ),
                  ),
                  Expanded(
                    child: Column(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Text(
                          widget.detail.title,
                          textAlign: TextAlign.center,
                          style: theme.textTheme.titleMedium?.copyWith(
                            fontFamily: detailFontFamily,
                            color: AppColors.gray4,
                            fontSize: 19,
                            fontWeight: FontWeight.w800,
                            height: 1.35,
                          ),
                        ),
                        const SizedBox(height: 4),
                        Text(
                          widget.detail.category.name,
                          textAlign: TextAlign.center,
                          style: theme.textTheme.bodyMedium?.copyWith(
                            fontFamily: detailFontFamily,
                            color: AppColors.darkGray,
                            fontWeight: FontWeight.w700,
                            fontSize: 15,
                          ),
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(width: 44, height: 44),
                ],
              ),
              const SizedBox(height: 20),
              Expanded(
                child: ScriptScrollView(
                  scripts: widget.scripts,
                  controller: widget.controller,
                ),
              ),
              const SizedBox(height: 12),
              SizedBox(
                height: 150,
                child: AudioControls(
                  controller: widget.controller,
                  onSeekRelative: widget.viewModel.seekRelative,
                  onTogglePlayback: widget.viewModel.togglePlayback,
                  onSpeedSelected: widget.viewModel.setSpeed,
                  speedOptions: widget.viewModel.speedOptions,
                  currentSpeed: widget.viewModel.currentSpeed,
                  speedLabel: widget.viewModel.speedLabel,
                  formatDuration: widget.viewModel.formatDuration,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class ScriptScrollView extends StatefulWidget {
  const ScriptScrollView({
    super.key,
    required this.scripts,
    required this.controller,
  });

  final List<ScriptLine> scripts;
  final HearitPlayerController controller;

  @override
  State<ScriptScrollView> createState() => _ScriptScrollViewState();
}

class _ScriptScrollViewState extends State<ScriptScrollView> {
  static const double _lineExtent = 44;
  final ScrollController _scrollController = ScrollController();
  int _currentIndex = 0;
  double _viewportHeight = 0;
  bool _autoFollow = true;

  @override
  void initState() {
    super.initState();
    _currentIndex = _findCurrentIndex(widget.controller.position);
    widget.controller.addListener(_handlePositionUpdate);
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _jumpToCurrent(forceJump: true);
    });
  }

  @override
  void didUpdateWidget(covariant ScriptScrollView oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (widget.controller != oldWidget.controller) {
      oldWidget.controller.removeListener(_handlePositionUpdate);
      widget.controller.addListener(_handlePositionUpdate);
      _currentIndex = _findCurrentIndex(widget.controller.position);
      WidgetsBinding.instance.addPostFrameCallback((_) {
        _jumpToCurrent(forceJump: true);
      });
    }
  }

  @override
  void dispose() {
    widget.controller.removeListener(_handlePositionUpdate);
    _scrollController.dispose();
    super.dispose();
  }

  void _handlePositionUpdate() {
    if (widget.scripts.isEmpty) return;
    final nextIndex = _findCurrentIndex(widget.controller.position);
    if (nextIndex == _currentIndex) return;
    setState(() {
      _currentIndex = nextIndex;
    });
    if (_autoFollow) {
      _jumpToCurrent();
    }
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

  void _updateViewportHeight(double height) {
    if (height == _viewportHeight) return;
    _viewportHeight = height;
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!mounted) return;
      _jumpToCurrent(forceJump: true);
      setState(() {});
    });
  }

  void _disableAutoFollow() {
    if (!_autoFollow) return;
    setState(() {
      _autoFollow = false;
    });
  }

  void _enableAutoFollow() {
    if (_autoFollow) return;
    setState(() {
      _autoFollow = true;
    });
  }

  void _jumpToCurrent({bool forceJump = false}) {
    if (!_scrollController.hasClients || _viewportHeight <= 0) return;
    final double target = (_lineExtent * _currentIndex)
        .clamp(0.0, _scrollController.position.maxScrollExtent);
    if (forceJump) {
      _scrollController.jumpTo(target);
      return;
    }
    _scrollController.animateTo(
      target,
      duration: const Duration(milliseconds: 420),
      curve: Curves.easeOutCubic,
    );
  }

  Future<void> _handleScriptTap(int index) async {
    if (index < 0 || index >= widget.scripts.length) return;
    final script = widget.scripts[index];

    _enableAutoFollow();
    setState(() {
      _currentIndex = index;
    });
    _jumpToCurrent();
    await widget.controller.seek(script.start);
  }

  @override
  Widget build(BuildContext context) {
    if (widget.scripts.isEmpty) return const SizedBox.shrink();
    final theme = Theme.of(context);
    final centerPadding =
        ((_viewportHeight - _lineExtent) / 2).clamp(0.0, double.infinity);

    return LayoutBuilder(
      builder: (context, constraints) {
        _updateViewportHeight(constraints.maxHeight);
        return NotificationListener<ScrollNotification>(
          onNotification: (notification) {
            final bool isUserScroll =
                (notification is ScrollStartNotification &&
                    notification.dragDetails != null) ||
                notification is UserScrollNotification ||
                (notification is ScrollUpdateNotification &&
                    notification.dragDetails != null);
            if (isUserScroll) {
              _disableAutoFollow();
            }
            return false;
          },
          child: ClipRect(
            child: ListView.builder(
              controller: _scrollController,
              physics: const ClampingScrollPhysics(),
              padding: EdgeInsets.symmetric(vertical: centerPadding),
              itemExtent: _lineExtent,
              itemCount: widget.scripts.length,
              itemBuilder: (context, index) {
                final script = widget.scripts[index];
                final bool isRead = index <= _currentIndex;
                final Color textColor =
                    isRead ? AppColors.gray4 : AppColors.darkGray;

                return GestureDetector(
                  behavior: HitTestBehavior.opaque,
                  onTap: () => _handleScriptTap(index),
                  child: Center(
                    child: Text(
                      script.text,
                      textAlign: TextAlign.center,
                      maxLines: 2,
                      overflow: TextOverflow.ellipsis,
                      style: theme.textTheme.bodyMedium?.copyWith(
                        fontFamily: detailFontFamily,
                        color: textColor,
                        fontWeight: FontWeight.w700,
                        fontSize: 15,
                        height: 1.35,
                      ),
                    ),
                  ),
                );
              },
            ),
          ),
        );
      },
    );
  }
}
