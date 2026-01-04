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

  /// 현재 재생 중인 항목으로 스마트 스크롤
  ///
  /// 항목이 이미 화면에 보이면 스크롤하지 않음.
  /// 보이지 않으면 최소한의 스크롤로 중앙에 위치시킴.
  void _scrollToCurrentItem() {
    final index = widget.playerController.currentPlaylistIndex;
    if (index < 0 || !_scrollController.hasClients) return;

    // 현재 스크롤 위치 정보
    final scrollOffset = _scrollController.offset;
    final viewportHeight = _scrollController.position.viewportDimension;

    // 항목 높이: padding(24) + content(64) = 88px
    const itemHeight = 88.0;

    // 현재 항목의 위치 계산
    final itemTop = index * itemHeight;
    final itemBottom = itemTop + itemHeight;

    // 현재 보이는 영역 계산
    final visibleTop = scrollOffset;
    final visibleBottom = scrollOffset + viewportHeight;

    // 항목이 이미 화면에 완전히 보이면 스크롤하지 않음
    if (itemTop >= visibleTop && itemBottom <= visibleBottom) {
      return;
    }

    // 항목을 화면 중앙에 위치시키기 위한 스크롤 위치 계산
    final targetOffset = (itemTop - viewportHeight / 2 + itemHeight / 2).clamp(
      0.0,
      _scrollController.position.maxScrollExtent,
    );

    _scrollController.animateTo(
      targetOffset,
      duration: const Duration(milliseconds: 300),
      curve: Curves.easeInOut,
    );
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
            color: AppColors.gray1,
            borderRadius: BorderRadius.only(
              topLeft: Radius.circular(20),
              topRight: Radius.circular(20),
            ),
          ),
          child: Column(
            children: [
              // Drawer 핸들 막대기
              Container(
                margin: const EdgeInsets.only(top: 12, bottom: 8),
                width: 40,
                height: 4,
                decoration: BoxDecoration(
                  color: AppColors.gray2,
                  borderRadius: BorderRadius.circular(2),
                ),
              ),
              const _PlaylistHeader(),
              Expanded(
                child: AnimatedBuilder(
                  animation: widget.playerController,
                  builder: (context, _) {
                    final playlist = widget.playerController.playlist;
                    final currentIndex =
                        widget.playerController.currentPlaylistIndex;

                    if (playlist.isEmpty) {
                      return Center(
                        child: Padding(
                          padding: const EdgeInsets.symmetric(horizontal: 40),
                          child: Column(
                            mainAxisAlignment: MainAxisAlignment.center,
                            children: [
                              Text(
                                '플레이리스트가 없습니다.',
                                style: TextStyle(
                                  color: AppColors.gray2,
                                  fontSize: 16,
                                  fontWeight: FontWeight.w600,
                                ),
                                textAlign: TextAlign.center,
                              ),
                              const SizedBox(height: 12),
                              Text(
                                '라이브러리에서 전체재생을 통해\n플레이리스트에 추가할 수 있습니다.',
                                style: TextStyle(
                                  color: AppColors.gray2,
                                  fontSize: 14,
                                  height: 1.5,
                                ),
                                textAlign: TextAlign.center,
                              ),
                            ],
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
                        final isCurrentItem = index == currentIndex;
                        final isActuallyPlaying =
                            widget.playerController.isPlaying;

                        return PlaylistItemCard(
                          item: item,
                          isCurrentItem: isCurrentItem,
                          isPlaying: isActuallyPlaying,
                          onTap: () {
                            // 카드 영역 클릭: 다른 항목이면 해당 항목 재생
                            if (!isCurrentItem) {
                              widget.playerController.playPlaylistItem(index);
                            }
                          },
                          onPlayPauseTap: () {
                            // 아이콘 클릭: 일시정지/재생 토글
                            if (isCurrentItem) {
                              // 현재 재생 중인 항목
                              if (isActuallyPlaying) {
                                widget.playerController.pause(); // 일시정지
                              } else {
                                widget.playerController.play(); // 재생 재개
                              }
                            } else {
                              // 다른 항목 → 해당 항목 재생
                              widget.playerController.playPlaylistItem(index);
                            }
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

/// 플레이리스트 헤더 (제목만 표시)
class _PlaylistHeader extends StatelessWidget {
  const _PlaylistHeader();

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            '재생목록',
            style: TextStyle(
              fontSize: 20,
              fontWeight: FontWeight.bold,
              color: AppColors.gray4,
            ),
          ),
          const SizedBox(height: 4),
          Text(
            '내가 북마크한 히어릿',
            style: TextStyle(fontSize: 14, color: AppColors.gray4),
          ),
        ],
      ),
    );
  }
}
