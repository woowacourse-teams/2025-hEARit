import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../core/audio/hearit_player_controller.dart';
import 'hearit_detail.dart';
import 'hearit_detail_viewmodel.dart';
import 'widgets/audio_controls.dart';
import 'widgets/detail_header.dart';
import 'widgets/hearit_info_card.dart';
import 'widgets/script_view.dart';
import 'widgets/source_card.dart';
import 'widgets/summary_card.dart';

class HearitDetailScreen extends StatefulWidget {
  const HearitDetailScreen({super.key, required this.detail});

  final HearitDetail detail;

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
    _viewModel.removeListener(_onViewModelUpdated);
    _viewModel.dispose();
    super.dispose();
  }

  void _onViewModelUpdated() {
    if (mounted) setState(() {});
  }

  @override
  Widget build(BuildContext context) {
    final detail = _viewModel.detail;
    return Scaffold(
      backgroundColor: const Color(0xFF1F1F1F),
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 24),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const SizedBox(height: 12),
              DetailHeader(
                categoryName: detail.category.name,
                onBack: () => Navigator.of(context).pop(),
                onShare: () {},
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
                          height: 160,
                          width: double.infinity,
                          child: AnimatedBuilder(
                            animation: Listenable.merge([
                              _viewModel,
                              _viewModel.playerController,
                            ]),
                            builder: (context, _) {
                              return ScriptView(
                                scripts: _viewModel.scripts,
                                position: _viewModel.playerController.position,
                              );
                            },
                          ),
                        ),
                      ),
                      const SizedBox(height: 4),
                      SizedBox(
                        height: 150,
                        child: AnimatedBuilder(
                          animation: _viewModel,
                          builder: (context, _) {
                            return AudioControls(
                              controller: _viewModel.playerController,
                              isBookmarked: _viewModel.isBookmarked,
                              onBookmarkToggle: _viewModel.toggleBookmark,
                              onSeekRelative: _viewModel.seekRelative,
                              onTogglePlayback: _viewModel.togglePlayback,
                              onSpeedTap: _viewModel.cycleSpeed,
                              speedLabel: _viewModel.speedLabel,
                              formatDuration: _viewModel.formatDuration,
                            );
                          },
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
          ),
        ),
      ),
    );
  }
}
