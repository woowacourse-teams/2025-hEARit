import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/foundation.dart';

import '../../core/presentation/widgets/script_view.dart';
import 'explore_viewmodel.dart';
import 'widgets/explore_widgets.dart';

class ExploreScreen extends StatefulWidget {
  const ExploreScreen({super.key});

  @override
  State<ExploreScreen> createState() => ExploreScreenState();
}

class ExploreScreenState extends State<ExploreScreen> {
  static ExploreScreenState? _activeInstance;
  static final ValueNotifier<double> _idleProgress = ValueNotifier<double>(0);
  late final ExploreViewModel _viewModel;
  late final PageController _pageController;
  int _currentIndex = 0;
  IconData? _centerStatusIcon;
  Timer? _overlayTimer;

  @override
  void initState() {
    super.initState();
    _viewModel = ExploreViewModel()..addListener(_onViewModelUpdated);
    _viewModel.setOnCompleted(_handleCompleted);
    _viewModel.loadInitial();
    _pageController = PageController();
    _activeInstance = this;
  }

  @override
  void dispose() {
    _overlayTimer?.cancel();
    if (_activeInstance == this) {
      _activeInstance = null;
    }
    _viewModel.removeListener(_onViewModelUpdated);
    _viewModel.dispose();
    _pageController.dispose();
    super.dispose();
  }

  static void pauseActiveAudio() {
    _activeInstance?._viewModel.pauseAudio();
  }

  static Future<void> setActivePlayback(bool enabled) async {
    await _activeInstance?._viewModel.setPlaybackEnabled(enabled);
  }

  static ValueListenable<double> progressListenable() {
    return _activeInstance?._viewModel.progressNotifier ?? _idleProgress;
  }

  static Future<void> seekToFraction(double fraction) async {
    await _activeInstance?._viewModel.seekToFraction(fraction);
  }

  static void beginUserSeek() {
    _activeInstance?._viewModel.beginUserSeek();
  }

  static void updateTempProgress(double fraction) {
    _activeInstance?._viewModel.updateTempProgress(fraction);
  }

  static Future<void> endUserSeek(double fraction) async {
    await _activeInstance?._viewModel.endUserSeek(fraction);
  }

  void _onViewModelUpdated() {
    if (mounted) setState(() {});
  }

  Future<void> _handleCompleted() async {
    final nextIndex = _currentIndex + 1;
    final prevLength = _viewModel.items.length;
    if (nextIndex < prevLength) {
      await _pageController.animateToPage(
        nextIndex,
        duration: const Duration(milliseconds: 280),
        curve: Curves.easeOut,
      );
      return;
    }
    await _viewModel.loadMore();
    if (_viewModel.items.length > prevLength) {
      await _pageController.animateToPage(
        nextIndex,
        duration: const Duration(milliseconds: 280),
        curve: Curves.easeOut,
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final active = _viewModel.activeItem;
    return Scaffold(
      backgroundColor: const Color(0xFF1F1F1F),
      body: SafeArea(
        child: GestureDetector(
          behavior: HitTestBehavior.translucent,
          onTap: () async {
            final wasPlaying = _viewModel.isPlaying;
            // Show intent immediately.
            _overlayTimer?.cancel();
            setState(() {
              _centerStatusIcon = wasPlaying
                  ? Icons.pause_rounded
                  : Icons.play_arrow_rounded;
            });
            _overlayTimer = Timer(const Duration(seconds: 1), () {
              if (mounted) {
                setState(() {
                  _centerStatusIcon = null;
                });
              }
            });

            await _viewModel.togglePlayPause();
          },
          child: LayoutBuilder(
            builder: (context, constraints) {
              if (_viewModel.initialLoading || active == null) {
                return const Center(
                  child: CircularProgressIndicator(color: Color(0xFFA86BFF)),
                );
              }
              const double horizontalPadding = 22;
              return PageView.builder(
                controller: _pageController,
                scrollDirection: Axis.vertical,
                itemCount: _viewModel.items.length,
                onPageChanged: (index) {
                  _viewModel.setActiveIndex(index);
                  _currentIndex = index;
                  if (index >= _viewModel.items.length - 3) {
                    _viewModel.loadMore();
                  }
                },
                itemBuilder: (context, index) {
                  final item = _viewModel.items[index];
                  return Stack(
                    children: [
                      Positioned(
                        top: 16,
                        left: horizontalPadding,
                        right: horizontalPadding,
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.center,
                          children: [
                            ExploreHighlightBanner(text: _viewModel.pitchLine),
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
                                assetPath: 'assets/images/explore_LP.png',
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
                                position: _viewModel.position,
                              ),
                            ),
                            const SizedBox(height: 8),
                            ExploreContinueButton(
                              onPressed: () {
                                ScaffoldMessenger.of(context).showSnackBar(
                                  const SnackBar(
                                    content: Text('팟캐스트 이어듣기 기능이 준비 중입니다.'),
                                    behavior: SnackBarBehavior.floating,
                                  ),
                                );
                              },
                            ),
                          ],
                        ),
                      ),
                      // Overlay painted last to stay on top of other content.
                      if (_centerStatusIcon != null)
                        Positioned.fill(
                          child: IgnorePointer(
                            child: Transform.translate(
                              offset: const Offset(0, 100),
                              child: Center(
                                child: Container(
                                  padding: const EdgeInsets.all(20),
                                  decoration: BoxDecoration(
                                    color: const Color(
                                      0xFF9533F5,
                                    ).withOpacity(0.7),
                                    shape: BoxShape.circle,
                                  ),
                                  child: Icon(
                                    _centerStatusIcon,
                                    size: 40,
                                    color: Colors.white,
                                  ),
                                ),
                              ),
                            ),
                          ),
                        ),
                    ],
                  );
                },
              );
            },
          ),
        ),
      ),
    );
  }
}
