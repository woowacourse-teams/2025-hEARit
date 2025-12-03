import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../core/audio/hearit_player_controller.dart';
import '../../core/presentation/widgets/script_view.dart';
import '../../core/theme/app_colors.dart';
import 'hearit_detail.dart';
import 'hearit_detail_viewmodel.dart';
import 'widgets/audio_controls.dart';
import 'widgets/detail_header.dart';
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

  Future<void> _stopPlayback() async {
    if (widget.pauseOnExit) {
      await _viewModel.playerController.pause();
    }
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
    return WillPopScope(
      onWillPop: _handleWillPop,
      child: Scaffold(
        backgroundColor: AppColors.hearitBlack,
        body: SafeArea(
          top: true,
          bottom: false,
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
                            ),
                            const SizedBox(height: 24),
                            Center(
                              child: SizedBox(
                                height: ScriptView.preferredHeight,
                                width: double.infinity,
                                child: ScriptView(
                                  scripts: _viewModel.scripts,
                                  position:
                                      _viewModel.playerController.position,
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
                              ),
                            ),
                            const SizedBox(height: 12),
                            SourceCard(sources: detail.sources),
                            const SizedBox(height: 12),
                            SummaryCard(
                              summary: detail.summary,
                              keywords: detail.keywords,
                            ),
                            const SizedBox(height: 12),
                          ],
                        ),
                      ),
                    ),
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
                _shimmerBox(height: ScriptView.preferredHeight), // ScriptView 영역
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
