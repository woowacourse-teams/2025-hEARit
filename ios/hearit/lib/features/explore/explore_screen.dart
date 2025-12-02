import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/foundation.dart';
import 'package:provider/provider.dart';

import '../../core/audio/hearit_player_controller.dart';
import '../../core/theme/app_colors.dart';
import '../detail/hearit_detail.dart';
import '../detail/hearit_detail_screen.dart';
import 'explore_viewmodel.dart';
import 'explore_models.dart';
import 'widgets/explore_feed_page.dart';

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
    _viewModel = ExploreViewModel(
      controller: context.read<HearitPlayerController>(),
    )..addListener(_onViewModelUpdated);
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

  Future<void> _refreshExplore() async {
    _overlayTimer?.cancel();
    _centerStatusIcon = null;
    _currentIndex = 0;
    if (_pageController.hasClients) {
      _pageController.jumpToPage(0);
    }
    await _viewModel.pauseAudio();
    await _viewModel.setPlaybackEnabled(false);
    await _viewModel.loadInitial();
  }

  Future<void> _openDetailFromExplore(ExploreFeedItem item) async {
    await _viewModel.pauseAudio();
    await _viewModel.setPlaybackEnabled(false);

    final stub = HearitDetail.fromSummaryStub(
      id: item.id,
      title: item.title,
      categoryName: '탐색',
      accentColor: item.categoryColor,
      createdAt: DateTime.now(),
      lastPlayTime: _viewModel.position,
    );

    if (!mounted) return;
    await Navigator.of(
      context,
    ).push(MaterialPageRoute(builder: (_) => HearitDetailScreen(detail: stub)));
  }

  @override
  Widget build(BuildContext context) {
    final active = _viewModel.activeItem;
    return Scaffold(
      backgroundColor: AppColors.hearitBlack,
      body: SafeArea(
        bottom: false,
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
              return RefreshIndicator(
                color: const Color(0xFFA86BFF),
                backgroundColor: AppColors.hearitBlack,
                onRefresh: _refreshExplore,
                child: PageView.builder(
                  controller: _pageController,
                  scrollDirection: Axis.vertical,
                  physics: const BouncingScrollPhysics(
                    parent: AlwaysScrollableScrollPhysics(),
                  ),
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
                    // Keep the continue card just above the slider/nav area.
                    const bottomSpacing = 28.0;
                    return ExploreFeedPage(
                      item: item,
                      pitchLine: _viewModel.pitchLine,
                      position: _viewModel.position,
                      centerStatusIcon: _centerStatusIcon,
                      horizontalPadding: horizontalPadding,
                      bottomSpacing: bottomSpacing,
                      onContinuePressed: () => _openDetailFromExplore(item),
                      isPlaying: _viewModel.isPlaying && index == _currentIndex,
                    );
                  },
                ),
              );
            },
          ),
        ),
      ),
    );
  }
}
