import 'package:flutter/material.dart';
import 'package:hearit/core/theme/app_colors.dart';

import '../../../core/audio/hearit_player_controller.dart';
import 'playlist_item_card.dart';

/// 플레이리스트를 보여주는 하단 Drawer
///
/// DraggableScrollableSheet를 사용하여 화면의 60%를 차지하며,
/// 현재 재생 중인 항목이 가장 위로 자동 스크롤됩니다.
class PlaylistDrawer extends StatefulWidget {
  final HearitPlayerController playerController;

  const PlaylistDrawer({super.key, required this.playerController});

  @override
  State<PlaylistDrawer> createState() => _PlaylistDrawerState();
}

class _PlaylistDrawerState extends State<PlaylistDrawer> {
  final ScrollController _scrollController = ScrollController();

  @override
  void initState() {
    super.initState();
    // 초기 로드 시 현재 재생 중인 항목으로 스크롤
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _scrollToCurrentItem();
    });
  }

  @override
  void dispose() {
    _scrollController.dispose();
    super.dispose();
  }

  /// 현재 재생 중인 항목으로 스크롤
  void _scrollToCurrentItem() {
    final index = widget.playerController.currentPlaylistIndex;
    if (index >= 0 && _scrollController.hasClients) {
      // 각 카드의 높이는 약 80px (추정)
      final offset = index * 80.0;
      _scrollController.animateTo(
        offset,
        duration: const Duration(milliseconds: 300),
        curve: Curves.easeInOut,
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return DraggableScrollableSheet(
      initialChildSize: 0.6,
      minChildSize: 0.3,
      maxChildSize: 0.9,
      builder: (context, scrollController) {
        return Container(
          decoration: const BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.only(
              topLeft: Radius.circular(20),
              topRight: Radius.circular(20),
            ),
          ),
          child: Column(
            children: [
              _PlaylistHeader(onClose: () => Navigator.pop(context)),
              const Divider(height: 1),
              Expanded(
                child: AnimatedBuilder(
                  animation: widget.playerController,
                  builder: (context, _) {
                    final playlist = widget.playerController.playlist;
                    final currentIndex =
                        widget.playerController.currentPlaylistIndex;

                    if (playlist.isEmpty) {
                      return const Center(
                        child: Text(
                          '플레이리스트가 비어있습니다',
                          style: TextStyle(
                            color: AppColors.darkGray,
                            fontSize: 14,
                          ),
                        ),
                      );
                    }

                    return ListView.builder(
                      controller: _scrollController,
                      padding: const EdgeInsets.symmetric(vertical: 8),
                      itemCount: playlist.length,
                      itemBuilder: (context, index) {
                        final item = playlist[index];
                        final isPlaying = index == currentIndex;

                        return PlaylistItemCard(
                          item: item,
                          isPlaying: isPlaying,
                          onTap: () {
                            widget.playerController.playPlaylistItem(index);
                            _scrollToCurrentItem();
                          },
                        );
                      },
                    );
                  },
                ),
              ),
            ],
          ),
        );
      },
    );
  }
}

/// 플레이리스트 헤더 (제목 + 닫기 버튼)
class _PlaylistHeader extends StatelessWidget {
  final VoidCallback onClose;

  const _PlaylistHeader({required this.onClose});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
      child: Row(
        children: [
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text(
                  '재생목록',
                  style: TextStyle(
                    fontSize: 20,
                    fontWeight: FontWeight.bold,
                    color: AppColors.hearitBlack,
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  '내가 북마크한 히어릿',
                  style: TextStyle(fontSize: 14, color: AppColors.darkGray),
                ),
              ],
            ),
          ),
          IconButton(
            icon: const Icon(Icons.close),
            onPressed: onClose,
            color: AppColors.hearitBlack,
          ),
        ],
      ),
    );
  }
}
