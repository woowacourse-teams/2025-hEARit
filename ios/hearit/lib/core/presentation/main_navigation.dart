import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import 'package:hearit/core/theme/app_colors.dart';
import '../audio/hearit_player_controller.dart';

import '../../features/detail/hearit_detail.dart';
import '../../features/detail/hearit_detail_screen.dart';
import '../../features/explore/explore_screen.dart';
import '../../features/home/home_screen.dart';
import '../../features/library/library_screen.dart';
import '../../features/library/widgets/playlist_drawer.dart';
import '../../features/search/search_screen.dart';

class MainNavigation extends StatefulWidget {
  const MainNavigation({super.key});

  @override
  State<MainNavigation> createState() => _MainNavigationState();
}

class _MainNavigationState extends State<MainNavigation> {
  int _currentIndex = 0;
  bool _isRestoringAudio = false; // 오디오 복원 중 상태
  final List<GlobalKey<NavigatorState>> _navigatorKeys = List.generate(
    4,
    (_) => GlobalKey<NavigatorState>(),
  );
  late final _TabRouteObserver _exploreRouteObserver;
  late final HearitPlayerController _playerController;

  static const List<_NavItem> _navItems = [
    _NavItem(label: '홈', icon: Icons.home),
    _NavItem(label: '검색', icon: Icons.search),
    _NavItem(label: '탐색', icon: Icons.explore),
    _NavItem(label: '라이브러리', icon: Icons.collections_bookmark),
  ];

  @override
  void initState() {
    super.initState();
    _playerController = context.read<HearitPlayerController>();
    _exploreRouteObserver = _TabRouteObserver(
      onStackChanged: _onExploreStackChanged,
    );

    // PlayerController 리스너 추가 (에러 처리용)
    _playerController.addListener(_onPlayerStateChanged);
  }

  @override
  void dispose() {
    _playerController.removeListener(_onPlayerStateChanged);
    super.dispose();
  }

  void _onPlayerStateChanged() {
    // 에러 상태 체크는 나중에 필요시 추가
  }

  void _onExploreStackChanged() {
    if (!mounted) return;
    setState(() {});
    final isExploreRoot = !(_navigatorKeys[2].currentState?.canPop() ?? false);
    if (_currentIndex == 2 && isExploreRoot) {
      ExploreScreenState.setActivePlayback(true);
    }
  }

  void _onItemTapped(int index) async {
    // ========================================
    // 탐색 화면에서 나가는 경우
    // ========================================
    if (_currentIndex == 2 && index != 2) {
      debugPrint('🔄 [탭전환] 탐색 → ${_navItems[index].label} (복원 시작)');

      // 탐색 미리듣기 일시정지
      if (ExploreScreenState.isPlaybackEnabled()) {
        ExploreScreenState.pauseActiveAudio();
        ExploreScreenState.setActivePlayback(false);
        debugPrint('⏸️ [탭전환] 탐색 미리듣기 일시정지');
      }

      // 즉시 화면 전환 (복원은 백그라운드)
      setState(() {
        _currentIndex = index;
        _isRestoringAudio = true; // 복원 시작
      });
      debugPrint('🎬 [탭전환] 화면 전환 완료, 복원 중 플래그: $_isRestoringAudio');

      // ✅ FIX: try-finally로 안전하게 복원 상태 해제
      try {
        debugPrint('⏳ [탭전환] restoreStateAfterExplore() 호출...');
        await _playerController.restoreStateAfterExplore();
        debugPrint('✅ [탭전환] restoreStateAfterExplore() 완료');
      } catch (e) {
        debugPrint('❌ [탭전환] 복원 중 예외 발생: $e');
      } finally {
        // 복원 완료 (성공/실패 관계없이 UI 상태 해제)
        if (mounted) {
          setState(() {
            _isRestoringAudio = false;
          });
          debugPrint('🏁 [탭전환] 복원 완료, 복원 중 플래그: $_isRestoringAudio');
        } else {
          debugPrint('⚠️ [탭전환] Widget unmounted, setState 스킵');
        }
      }
      return;
    }

    // ========================================
    // 탐색 화면으로 진입하는 경우
    // ========================================
    if (index == 2 && _currentIndex != 2) {
      // 현재 재생 상태 저장
      await _playerController.saveStateBeforeExplore();

      // 기존 재생 중인 오디오 일시정지
      if (_playerController.isPlaying) {
        await _playerController.pause();
      }
    }

    // ========================================
    // 같은 탭 다시 탭 (루트로 이동)
    // ========================================
    if (_currentIndex == index) {
      _navigatorKeys[index].currentState?.popUntil((route) => route.isFirst);
      return;
    }

    // ========================================
    // 다른 탭으로 전환
    // ========================================
    setState(() {
      _currentIndex = index;
    });

    // 탐색 화면 진입 시 미리듣기 활성화
    if (index == 2) {
      ExploreScreenState.setActivePlayback(true);
    }
  }

  bool _isCurrentRouteDetail(int tabIndex) {
    final navigator = _navigatorKeys[tabIndex].currentState;
    if (navigator == null) return false;

    bool isDetail = false;
    navigator.popUntil((route) {
      isDetail = route.settings.name == '/detail';
      return true; // 현재 route만 확인하고 바로 복귀
    });
    return isDetail;
  }

  Future<void> _openDetailFromMini() async {
    final media = _playerController.currentMediaItem;
    if (media == null) return;
    final idString = media.id.replaceFirst('hearit-', '');
    final id = int.tryParse(idString);
    if (id == null) return;
    final detail = HearitDetail.fromSummaryStub(
      id: id,
      title: media.title,
      categoryName: media.album ?? media.artist ?? '히어릿',
      accentColor: AppColors.hearitPurple2,
      createdAt: DateTime.now(),
      lastPlayTime: _playerController.position,
      playTime: _playerController.duration, // 복원된 duration 전달
    );
    if (!mounted) return;
    await Navigator.of(context).push(
      MaterialPageRoute(
        builder: (_) => HearitDetailScreen(detail: detail, pauseOnExit: false),
        settings: const RouteSettings(name: '/detail'),
      ),
    );
  }

  void _openPlaylistDrawer() {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (context) => PlaylistDrawer(playerController: _playerController),
    );
  }

  Future<bool> _onWillPop() async {
    final NavigatorState currentNav =
        _navigatorKeys[_currentIndex].currentState!;
    if (currentNav.canPop()) {
      currentNav.pop();
      return false;
    }
    return true;
  }

  @override
  Widget build(BuildContext context) {
    return WillPopScope(
      onWillPop: _onWillPop,
      child: Scaffold(
        backgroundColor: AppColors.hearitBlack,
        extendBody: true,
        body: IndexedStack(
          index: _currentIndex,
          children: [
            _TabNavigator(
              navigatorKey: _navigatorKeys[0],
              builder: (_) => HomeScreen(onExploreTap: () => _onItemTapped(2)),
            ),
            _TabNavigator(
              navigatorKey: _navigatorKeys[1],
              builder: (_) =>
                  SearchScreen(onBackToHome: () => _onItemTapped(0)),
            ),
            _TabNavigator(
              navigatorKey: _navigatorKeys[2],
              builder: (_) => const ExploreScreen(),
              observers: [_exploreRouteObserver],
            ),
            _TabNavigator(
              navigatorKey: _navigatorKeys[3],
              builder: (_) => const LibraryScreen(),
            ),
          ],
        ),
        bottomNavigationBar: _buildBottomNavigationBar(),
      ),
    );
  }

  Widget _buildBottomNavigationBar() {
    return AnimatedBuilder(
      animation: _playerController,
      builder: (context, child) {
        // 재생바 표시 여부 판단
        final canPopCurrent =
            _navigatorKeys[_currentIndex].currentState?.canPop() ?? false;
        final hasMediaItem = _playerController.currentMediaItem != null;

        // 탐색 미리듣기는 미니플레이어 표시하지 않음
        final isExplorePreviewing = _playerController.isPlayingExplorePreview;

        // detail 화면인지 확인
        final isDetailScreen =
            canPopCurrent && _isCurrentRouteDetail(_currentIndex);

        // 검색 탭(index=1)에서는 스택이 있어도 미니플레이어 표시
        // 단, detail 화면이거나 탐색 미리듣기 중에는 표시하지 않음
        // 복원 중에도 미니플레이어 표시 (스켈레톤)
        final showMiniPlayer =
            _currentIndex != 2 &&
            !isDetailScreen && // detail 화면이면 미니플레이어 숨김
            (_currentIndex == 1 || !canPopCurrent) &&
            (hasMediaItem || _isRestoringAudio) &&
            !isExplorePreviewing;

        return Container(
          decoration: BoxDecoration(
            color: AppColors.gray1,
            borderRadius: showMiniPlayer
                ? const BorderRadius.only(
                    topLeft: Radius.circular(8),
                    topRight: Radius.circular(8),
                  )
                : BorderRadius.zero,
          ),
          child: SafeArea(
            top: false,
            bottom: true,
            child: MediaQuery.removePadding(
              context: context,
              removeBottom: false,
              child: MediaQuery.removeViewInsets(
                context: context,
                removeTop: false,
                removeLeft: false,
                removeRight: false,
                removeBottom: false,
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    _currentIndex == 2
                        ? ValueListenableBuilder<double>(
                            valueListenable:
                                ExploreScreenState.progressListenable(),
                            builder: (context, value, _) {
                              final isExploreRoot =
                                  !(_navigatorKeys[2].currentState?.canPop() ??
                                      false);
                              if (!isExploreRoot) {
                                return const SizedBox.shrink();
                              }
                              return SliderTheme(
                                data: SliderTheme.of(context).copyWith(
                                  trackHeight: 5,
                                  thumbShape: const RoundSliderThumbShape(
                                    enabledThumbRadius: 0,
                                  ),
                                  overlayShape: SliderComponentShape.noOverlay,
                                  activeTrackColor: AppColors.hearitPurple2,
                                  inactiveTrackColor: AppColors.gray2,
                                  thumbColor: Colors.transparent,
                                ),
                                child: Slider(
                                  value: value.clamp(0.0, 1.0),
                                  onChanged:
                                      ExploreScreenState.updateTempProgress,
                                  onChangeStart: (_) =>
                                      ExploreScreenState.beginUserSeek(),
                                  onChangeEnd: (v) =>
                                      ExploreScreenState.endUserSeek(v),
                                ),
                              );
                            },
                          )
                        : AnimatedBuilder(
                            animation: _playerController,
                            builder: (context, _) {
                              final canPopCurrent =
                                  _navigatorKeys[_currentIndex].currentState
                                      ?.canPop() ??
                                  false;

                              // detail 화면인지 확인
                              final isDetailScreen =
                                  canPopCurrent &&
                                  _isCurrentRouteDetail(_currentIndex);

                              // 검색 탭(index=1)에서는 스택이 있어도 미니플레이어 표시
                              // 단, detail 화면이거나 탐색 미리듣기 중에는 표시하지 않음
                              if (isDetailScreen ||
                                  (canPopCurrent && _currentIndex != 1)) {
                                return const SizedBox.shrink();
                              }

                              // 탐색 미리듣기 중에는 미니플레이어 숨김
                              if (_playerController.isPlayingExplorePreview) {
                                return const SizedBox.shrink();
                              }

                              // 복원 중이면 스켈레톤 표시
                              if (_isRestoringAudio) {
                                return const _MiniPlayerSkeleton();
                              }

                              final media = _playerController.currentMediaItem;
                              if (media == null) {
                                return const SizedBox.shrink();
                              }
                              final durationMs =
                                  _playerController.duration.inMilliseconds;
                              final positionMs =
                                  _playerController.position.inMilliseconds;
                              final progress = durationMs > 0
                                  ? (positionMs / durationMs).clamp(0.0, 1.0)
                                  : 0.0;
                              return _DetailMiniPlayerBar(
                                title: media.title,
                                progress: progress,
                                durationMs: durationMs,
                                isPlaying: _playerController.isPlaying,
                                isPlayingFromPlaylist:
                                    _playerController.isPlayingFromPlaylist,
                                isLoadingAudio:
                                    _playerController.isLoadingAudio,
                                onTogglePlay: () =>
                                    _playerController.togglePlayback(),
                                onSeekFraction: (fraction) {
                                  if (durationMs <= 0) return;
                                  final target = Duration(
                                    milliseconds: (durationMs * fraction)
                                        .round(),
                                  );
                                  _playerController.seek(target);
                                },
                                onTap: _openDetailFromMini,
                                onPlaylistTap: _openPlaylistDrawer,
                              );
                            },
                          ),
                    Padding(
                      padding: const EdgeInsets.symmetric(horizontal: 20),
                      child: SizedBox(
                        height: 60,
                        child: Row(
                          children: List.generate(_navItems.length, (index) {
                            final item = _navItems[index];
                            final isSelected = _currentIndex == index;
                            return Expanded(
                              child: _NavTapTarget(
                                onTap: () => _onItemTapped(index),
                                child: _NavVisual(
                                  icon: item.icon,
                                  label: item.label,
                                  color: isSelected
                                      ? AppColors.hearitPurple1
                                      : AppColors.gray4,
                                  iconSize: 30,
                                ),
                              ),
                            );
                          }),
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ),
        );
      },
    );
  }
}

class _TabNavigator extends StatelessWidget {
  const _TabNavigator({
    required this.navigatorKey,
    required this.builder,
    this.observers,
  });

  final GlobalKey<NavigatorState> navigatorKey;
  final WidgetBuilder builder;
  final List<NavigatorObserver>? observers;

  @override
  Widget build(BuildContext context) {
    return Navigator(
      key: navigatorKey,
      observers: observers ?? const [],
      onGenerateRoute: (settings) {
        return MaterialPageRoute(builder: builder, settings: settings);
      },
    );
  }
}

class _TabRouteObserver extends NavigatorObserver {
  _TabRouteObserver({required this.onStackChanged});

  final VoidCallback onStackChanged;

  void _notify() {
    WidgetsBinding.instance.addPostFrameCallback((_) => onStackChanged());
  }

  @override
  void didPush(Route route, Route? previousRoute) {
    super.didPush(route, previousRoute);
    _notify();
  }

  @override
  void didPop(Route route, Route? previousRoute) {
    super.didPop(route, previousRoute);
    _notify();
  }

  @override
  void didRemove(Route route, Route? previousRoute) {
    super.didRemove(route, previousRoute);
    _notify();
  }

  @override
  void didReplace({Route? newRoute, Route? oldRoute}) {
    super.didReplace(newRoute: newRoute, oldRoute: oldRoute);
    _notify();
  }
}

class _NavItem {
  const _NavItem({required this.label, required this.icon});

  final String label;
  final IconData icon;
}

class _NavVisual extends StatelessWidget {
  const _NavVisual({
    required this.icon,
    required this.label,
    required this.color,
    required this.iconSize,
  });

  final IconData icon;
  final String label;
  final Color color;
  final double iconSize;

  @override
  Widget build(BuildContext context) {
    return Column(
      mainAxisAlignment: MainAxisAlignment.end,
      children: [
        Padding(
          padding: const EdgeInsets.only(top: 12),
          child: Icon(icon, size: iconSize, color: color),
        ),
        const SizedBox(height: 6),
        Text(
          label,
          textHeightBehavior: const TextHeightBehavior(
            applyHeightToFirstAscent: false,
            applyHeightToLastDescent: false,
          ),
          strutStyle: const StrutStyle(
            fontSize: 12,
            height: 1.0,
            leading: 0,
            forceStrutHeight: true,
          ),
          style: TextStyle(
            color: color,
            fontSize: 12,
            fontWeight: FontWeight.w500,
            height: 1.0,
          ),
        ),
      ],
    );
  }
}

class _NavTapTarget extends StatelessWidget {
  const _NavTapTarget({required this.onTap, required this.child});

  final VoidCallback onTap;
  final Widget child;

  @override
  Widget build(BuildContext context) {
    return Material(
      color: Colors.transparent,
      child: InkWell(
        borderRadius: BorderRadius.circular(12),
        onTap: onTap,
        child: Padding(padding: const EdgeInsets.only(bottom: 0), child: child),
      ),
    );
  }
}

class _DetailMiniPlayerBar extends StatefulWidget {
  const _DetailMiniPlayerBar({
    required this.title,
    required this.progress,
    required this.durationMs,
    required this.isPlaying,
    required this.isPlayingFromPlaylist,
    required this.isLoadingAudio,
    required this.onTogglePlay,
    required this.onTap,
    required this.onSeekFraction,
    required this.onPlaylistTap,
  });

  final String title;
  final double progress;
  final int durationMs;
  final bool isPlaying;
  final bool isPlayingFromPlaylist;
  final bool isLoadingAudio;
  final VoidCallback onTogglePlay;
  final VoidCallback onTap;
  final ValueChanged<double> onSeekFraction;
  final VoidCallback onPlaylistTap;

  @override
  State<_DetailMiniPlayerBar> createState() => _DetailMiniPlayerBarState();
}

class _DetailMiniPlayerBarState extends State<_DetailMiniPlayerBar> {
  double? _dragValue;

  @override
  Widget build(BuildContext context) {
    final displayProgress = (_dragValue ?? widget.progress).clamp(0.0, 1.0);
    final canSeek = widget.durationMs > 0;
    return GestureDetector(
      behavior: HitTestBehavior.opaque,
      onTap: widget.onTap,
      child: Padding(
        padding: const EdgeInsets.fromLTRB(0, 12, 0, 8),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          mainAxisSize: MainAxisSize.min,
          children: [
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16),
              child: Row(
                children: [
                  Expanded(
                    child: Text(
                      widget.title,
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                      style: const TextStyle(
                        color: AppColors.gray4,
                        fontSize: 16,
                        fontWeight: FontWeight.w700,
                      ),
                    ),
                  ),
                  const SizedBox(width: 12),
                  _PlayPauseButton(
                    isPlaying: widget.isPlaying,
                    isLoading: widget.isLoadingAudio,
                    onToggle: widget.onTogglePlay,
                  ),
                  const SizedBox(width: 8),
                  _PlaylistButton(
                    onTap: widget.onPlaylistTap,
                    isActive: widget.isPlayingFromPlaylist,
                  ),
                ],
              ),
            ),
            const SizedBox(height: 10),
            SliderTheme(
              data: SliderTheme.of(context).copyWith(
                trackHeight: 5,
                thumbShape: const RoundSliderThumbShape(enabledThumbRadius: 0),
                overlayShape: SliderComponentShape.noOverlay,
                activeTrackColor: AppColors.hearitPurple2,
                inactiveTrackColor: AppColors.gray2,
                thumbColor: Colors.transparent,
              ),
              child: Slider(
                value: displayProgress,
                onChanged: canSeek
                    ? (v) => setState(() {
                        _dragValue = v;
                      })
                    : null,
                onChangeEnd: canSeek
                    ? (v) {
                        setState(() {
                          _dragValue = null;
                        });
                        widget.onSeekFraction(v);
                      }
                    : null,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _PlayPauseButton extends StatelessWidget {
  const _PlayPauseButton({
    required this.isPlaying,
    required this.isLoading,
    required this.onToggle,
  });

  final bool isPlaying;
  final bool isLoading;
  final VoidCallback onToggle;

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: isLoading ? null : onToggle,
      child: SizedBox(
        width: 44,
        height: 44,
        child: isLoading
            ? const Center(
                child: SizedBox(
                  width: 24,
                  height: 24,
                  child: CircularProgressIndicator(
                    strokeWidth: 2.5,
                    valueColor: AlwaysStoppedAnimation<Color>(AppColors.gray4),
                  ),
                ),
              )
            : Icon(
                isPlaying ? Icons.pause_rounded : Icons.play_arrow_rounded,
                color: AppColors.gray4,
                size: 32,
              ),
      ),
    );
  }
}

class _PlaylistButton extends StatelessWidget {
  const _PlaylistButton({required this.onTap, required this.isActive});

  final VoidCallback onTap;
  final bool isActive;

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: SizedBox(
        width: 44,
        height: 44,
        child: Icon(
          Icons.queue_music_rounded,
          color: AppColors.gray4,
          size: 28,
        ),
      ),
    );
  }
}

/// 미니플레이어 스켈레톤 UI (오디오 복원 중 표시)
class _MiniPlayerSkeleton extends StatelessWidget {
  const _MiniPlayerSkeleton();

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(0, 12, 0, 8),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisSize: MainAxisSize.min,
        children: [
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16),
            child: Row(
              children: [
                // 제목 스켈레톤
                Expanded(
                  child: Container(
                    height: 16,
                    decoration: BoxDecoration(
                      color: AppColors.gray2.withOpacity(0.3),
                      borderRadius: BorderRadius.circular(4),
                    ),
                  ),
                ),
                const SizedBox(width: 12),
                // 재생 버튼 스켈레톤 (로딩 스피너)
                Container(
                  width: 44,
                  height: 44,
                  decoration: BoxDecoration(
                    color: AppColors.gray2.withOpacity(0.3),
                    shape: BoxShape.circle,
                  ),
                  child: const Center(
                    child: SizedBox(
                      width: 20,
                      height: 20,
                      child: CircularProgressIndicator(
                        strokeWidth: 2,
                        valueColor: AlwaysStoppedAnimation<Color>(
                          AppColors.gray3,
                        ),
                      ),
                    ),
                  ),
                ),
                const SizedBox(width: 8),
                // 플레이리스트 버튼 스켈레톤
                Container(
                  width: 44,
                  height: 44,
                  decoration: BoxDecoration(
                    color: AppColors.gray2.withOpacity(0.3),
                    shape: BoxShape.circle,
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 10),
          // 슬라이더 스켈레톤
          SliderTheme(
            data: SliderTheme.of(context).copyWith(
              trackHeight: 5,
              thumbShape: const RoundSliderThumbShape(enabledThumbRadius: 0),
              overlayShape: SliderComponentShape.noOverlay,
              activeTrackColor: AppColors.gray2.withOpacity(0.3),
              inactiveTrackColor: AppColors.gray2.withOpacity(0.1),
              thumbColor: Colors.transparent,
            ),
            child: const Slider(value: 0.0, onChanged: null),
          ),
        ],
      ),
    );
  }
}
