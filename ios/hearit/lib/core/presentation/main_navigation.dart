import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import 'package:hearit/core/theme/app_colors.dart';
import '../audio/hearit_player_controller.dart';

import '../../features/detail/hearit_detail.dart';
import '../../features/detail/hearit_detail_screen.dart';
import '../../features/explore/explore_screen.dart';
import '../../features/home/home_screen.dart';
import '../../features/search/search_screen.dart';
import '../../features/setting/setting_screen.dart';

class MainNavigation extends StatefulWidget {
  const MainNavigation({super.key});

  @override
  State<MainNavigation> createState() => _MainNavigationState();
}

class _MainNavigationState extends State<MainNavigation> {
  int _currentIndex = 0;
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
    _NavItem(label: '설정', icon: Icons.settings),
  ];

  @override
  void initState() {
    super.initState();
    _playerController = context.read<HearitPlayerController>();
    _exploreRouteObserver = _TabRouteObserver(
      onStackChanged: _onExploreStackChanged,
    );
  }

  void _onExploreStackChanged() {
    if (!mounted) return;
    setState(() {});
    final isExploreRoot = !(_navigatorKeys[2].currentState?.canPop() ?? false);
    if (_currentIndex == 2 && isExploreRoot) {
      ExploreScreenState.setActivePlayback(true);
    }
  }

  void _onItemTapped(int index) {
    if (_currentIndex == 2 && index != 2) {
      // Pause explore preview audio only if it is currently active.
      if (ExploreScreenState.isPlaybackEnabled()) {
        ExploreScreenState.pauseActiveAudio();
        ExploreScreenState.setActivePlayback(false);
      }
    }
    if (index == 2 && _currentIndex != 2) {
      // Avoid overlapping with detail playback when entering explore.
      if (_playerController.isPlaying) {
        _playerController.pause();
      }
    }
    if (_currentIndex == index) {
      _navigatorKeys[index].currentState?.popUntil((route) => route.isFirst);
    } else {
      setState(() {
        _currentIndex = index;
      });
      if (index == 2) {
        ExploreScreenState.setActivePlayback(true);
      }
    }
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
    );
    if (!mounted) return;
    await Navigator.of(context).push(
      MaterialPageRoute(
        builder: (_) => HearitDetailScreen(detail: detail, pauseOnExit: false),
      ),
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
        body: Column(
          children: [
            Expanded(
              child: IndexedStack(
                index: _currentIndex,
                children: [
                  _TabNavigator(
                    navigatorKey: _navigatorKeys[0],
                    builder: (_) =>
                        HomeScreen(onExploreTap: () => _onItemTapped(2)),
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
                    builder: (_) =>
                        SettingScreen(onBackToHome: () => _onItemTapped(0)),
                  ),
                ],
              ),
            ),
            SafeArea(
              top: false,
              bottom: false,
              child: MediaQuery.removePadding(
                context: context,
                removeBottom: true,
                child: Container(
                  color: AppColors.gray1,
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      _currentIndex == 2
                          ? ValueListenableBuilder<double>(
                              valueListenable:
                                  ExploreScreenState.progressListenable(),
                              builder: (context, value, _) {
                                final isExploreRoot =
                                    !(_navigatorKeys[2].currentState
                                            ?.canPop() ??
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
                                    overlayShape:
                                        SliderComponentShape.noOverlay,
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
                                if (canPopCurrent) {
                                  return const SizedBox.shrink();
                                }
                                final media =
                                    _playerController.currentMediaItem;
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
                                );
                              },
                            ),
                      Padding(
                        padding: const EdgeInsets.fromLTRB(20, 0, 20, 12),
                        child: BottomNavigationBar(
                          backgroundColor: Colors.transparent,
                          elevation: 0,
                          type: BottomNavigationBarType.fixed,
                          currentIndex: _currentIndex,
                          onTap: _onItemTapped,
                          showSelectedLabels: false,
                          showUnselectedLabels: false,
                          items: _navItems
                              .map(
                                (item) => BottomNavigationBarItem(
                                  icon: _NavVisual(
                                    icon: item.icon,
                                    label: item.label,
                                    color: AppColors.gray4,
                                    iconSize: 34,
                                  ),
                                  activeIcon: _NavVisual(
                                    icon: item.icon,
                                    label: item.label,
                                    color: AppColors.hearitPurple1,
                                    iconSize: 34,
                                  ),
                                  label: item.label,
                                ),
                              )
                              .toList(),
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
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
      mainAxisSize: MainAxisSize.min,
      mainAxisAlignment: MainAxisAlignment.end,
      children: [
        Icon(icon, size: iconSize, color: color),
        const SizedBox(height: 4),
        Text(
          label,
          textHeightBehavior: const TextHeightBehavior(
            applyHeightToFirstAscent: false,
            applyHeightToLastDescent: false,
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

class _DetailMiniPlayerBar extends StatefulWidget {
  const _DetailMiniPlayerBar({
    required this.title,
    required this.progress,
    required this.durationMs,
    required this.isPlaying,
    required this.onTogglePlay,
    required this.onTap,
    required this.onSeekFraction,
  });

  final String title;
  final double progress;
  final int durationMs;
  final bool isPlaying;
  final VoidCallback onTogglePlay;
  final VoidCallback onTap;
  final ValueChanged<double> onSeekFraction;

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
                    onToggle: widget.onTogglePlay,
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
  const _PlayPauseButton({required this.isPlaying, required this.onToggle});

  final bool isPlaying;
  final VoidCallback onToggle;

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onToggle,
      child: Container(
        width: 44,
        height: 44,
        decoration: BoxDecoration(
          color: AppColors.gray1,
          shape: BoxShape.circle,
          boxShadow: [
            BoxShadow(
              color: Colors.black.withOpacity(0.2),
              blurRadius: 6,
              offset: const Offset(0, 2),
            ),
          ],
        ),
        child: Icon(
          isPlaying ? Icons.pause_rounded : Icons.play_arrow_rounded,
          color: AppColors.gray4,
          size: 28,
        ),
      ),
    );
  }
}
