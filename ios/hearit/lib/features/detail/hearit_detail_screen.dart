import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../core/analytics/analytics_event_names.dart';
import '../../core/analytics/analytics_param_keys.dart';
import '../../core/analytics/analytics_provider.dart';

import '../../core/audio/hearit_player_controller.dart';
import '../../core/presentation/widgets/script_view.dart';
import '../../core/theme/app_colors.dart';
import 'hearit_detail.dart';
import 'hearit_detail_viewmodel.dart';
import 'widgets/audio_controls.dart';
import 'widgets/detail_header.dart';
import 'widgets/full_script_view.dart';
import 'widgets/hearit_info_card.dart';
import 'widgets/source_card.dart';
import 'widgets/summary_card.dart';

class HearitDetailScreen extends StatefulWidget {
  const HearitDetailScreen({
    super.key,
    required this.detail,
    this.pauseOnExit = false,
  });

  final HearitDetail detail;
  final bool pauseOnExit;

  @override
  State<HearitDetailScreen> createState() => _HearitDetailScreenState();
}

class _HearitDetailScreenState extends State<HearitDetailScreen> {
  late final HearitDetailViewModel _viewModel;

  @override
  void initState() {
    super.initState();
    _viewModel = HearitDetailViewModel(
      detail: widget.detail,
      playerController: context.read<HearitPlayerController>(),
    )..addListener(_onViewModelUpdated);
    _viewModel.loadAll();
    WidgetsBinding.instance.addPostFrameCallback((_) => _logScreenView());
  }

  @override
  void dispose() {
    if (widget.pauseOnExit) {
      // Stop audio when leaving the detail screen so explore preview can resume cleanly.
      _viewModel.playerController.pause();
    }
    _viewModel.removeListener(_onViewModelUpdated);
    _viewModel.dispose();
    super.dispose();
  }

  void _onViewModelUpdated() {
    if (mounted) setState(() {});
  }

  void _logScreenView() {
    AnalyticsProvider.logger.logEvent(
      'screen_view',
      params: {
        AnalyticsParamKeys.screenName: AnalyticsParamKeys.screenNameDetail,
        AnalyticsParamKeys.screenClass: 'HearitDetailScreen',
      },
    );
  }

  void _onKeywordTap(String keyword) {
    AnalyticsProvider.logger.logEvent(
      AnalyticsEventNames.detailKeywordSelected,
      params: {AnalyticsParamKeys.keywordName: keyword},
    );
  }

  void _onSourceTap(HearitSource source) {
    AnalyticsProvider.logger.logEvent(
      AnalyticsEventNames.detailSourceSelected,
      params: {AnalyticsParamKeys.sourceName: source.sourceName},
    );
  }

  Future<void> _stopPlayback() async {
    if (widget.pauseOnExit) {
      await _viewModel.playerController.pause();
    }
  }

  Future<void> _showFullScript() async {
    if (_viewModel.scripts.isEmpty) return;
    final bool isTablet = MediaQuery.of(context).size.shortestSide >= 600;
    await showModalBottomSheet<void>(
      context: context,
      isScrollControlled: true,
      useRootNavigator: true,
      useSafeArea: true,
      backgroundColor: Colors.transparent,
      barrierColor: AppColors.hearitBlack,
      builder: (context) {
        return FractionallySizedBox(
          heightFactor: 1,
          child: FullScriptView(
            detail: _viewModel.detail,
            scripts: _viewModel.scripts,
            controller: _viewModel.playerController,
            viewModel: _viewModel,
            isTablet: isTablet,
          ),
        );
      },
    );
  }

  Future<bool> _handleWillPop() async {
    await _stopPlayback();
    return true;
  }

  Future<void> _handleBackTap() async {
    await _stopPlayback();
    if (mounted) {
      Navigator.of(context).pop();
    }
  }

  @override
  Widget build(BuildContext context) {
    final detail = _viewModel.detail;
    final size = MediaQuery.of(context).size;
    final bool isTablet = size.shortestSide >= 600;
    final double scriptHeight = isTablet
        ? ScriptView.tabletPreferredHeight
        : ScriptView.preferredHeight;
    return WillPopScope(
      onWillPop: _handleWillPop,
      child: Scaffold(
        backgroundColor: AppColors.hearitBlack,
        body: SafeArea(
          top: true,
          bottom: true,
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 24),
            child: AnimatedBuilder(
              animation: Listenable.merge([
                _viewModel,
                _viewModel.playerController,
              ]),
              builder: (context, _) {
                if (_viewModel.isInitialLoading) {
                  return _Skeleton(detail: detail);
                }
                return Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    DetailHeader(
                      categoryName: detail.category.name,
                      onBack: _handleBackTap,
                      isTablet: isTablet,
                    ),
                    const SizedBox(height: 12),
                    Expanded(
                      child: SingleChildScrollView(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            HearitInfoCard(
                              detail: detail,
                              formatDate: _viewModel.formatDate,
                              isTablet: isTablet,
                            ),
                            const SizedBox(height: 24),
                            Center(
                              child: Material(
                                color: Colors.transparent,
                                child: InkWell(
                                  onTap: _showFullScript,
                                  borderRadius: BorderRadius.circular(12),
                                  child: SizedBox(
                                    height: scriptHeight,
                                    width: double.infinity,
                                    child: ScriptView(
                                      scripts: _viewModel.scripts,
                                      position:
                                          _viewModel.playerController.position,
                                      isTablet: isTablet,
                                    ),
                                  ),
                                ),
                              ),
                            ),
                            const SizedBox(height: 4),
                            SizedBox(
                              height: 150,
                              child: AudioControls(
                                controller: _viewModel.playerController,
                                onSeekRelative: _viewModel.seekRelative,
                                onTogglePlayback: _viewModel.togglePlayback,
                                onSpeedSelected: _viewModel.setSpeed,
                                speedOptions: _viewModel.speedOptions,
                                currentSpeed: _viewModel.currentSpeed,
                                speedLabel: _viewModel.speedLabel,
                                formatDuration: _viewModel.formatDuration,
                                isTablet: isTablet,
                                isBookmarked: _viewModel.isBookmarked,
                                onBookmarkToggle: () =>
                                    _viewModel.toggleBookmark(context),
                              ),
                            ),
                            const SizedBox(height: 12),
                            SourceCard(
                              sources: detail.sources,
                              onSourceTap: _onSourceTap,
                              isTablet: isTablet,
                            ),
                            const SizedBox(height: 12),
                            SummaryCard(
                              summary: detail.summary,
                              keywords: detail.keywords,
                              onKeywordTap: _onKeywordTap,
                              isTablet: isTablet,
                            ),
                            const SizedBox(height: 20),
                          ],
                        ),
                      ),
                    ),
                    const SizedBox(height: 4),
                  ],
                );
              },
            ),
          ),
        ),
      ),
    );
  }
}

class _Skeleton extends StatelessWidget {
  const _Skeleton({required this.detail});

  final HearitDetail detail;

  @override
  Widget build(BuildContext context) {
    final size = MediaQuery.of(context).size;
    final bool isTablet = size.shortestSide >= 600;
    final double scriptHeight = isTablet
        ? ScriptView.tabletPreferredHeight
        : ScriptView.preferredHeight;
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const SizedBox(height: 12),
        DetailHeader(
          categoryName: detail.category.name,
          onBack: () => Navigator.of(context).pop(),
        ),
        const SizedBox(height: 12),
        Expanded(
          child: SingleChildScrollView(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                _shimmerBox(height: 360), // HearitInfoCard 영역
                const SizedBox(height: 24),
                _shimmerBox(height: scriptHeight), // ScriptView 영역
                const SizedBox(height: 12),
                _shimmerBox(height: 200), // AudioControls 영역
                const SizedBox(height: 16),
                _shimmerBox(height: 110), // SourceCard 영역
                const SizedBox(height: 12),
                _shimmerBox(height: 140), // SummaryCard 영역
                const SizedBox(height: 12),
              ],
            ),
          ),
        ),
      ],
    );
  }

  Widget _shimmerBox({required double height}) {
    return Container(
      height: height,
      width: double.infinity,
      decoration: BoxDecoration(
        color: const Color(0xFF2B2B2B),
        borderRadius: BorderRadius.circular(12),
      ),
    );
  }
}
