import 'package:flutter/material.dart';
import 'package:hearit/core/theme/app_colors.dart';

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

  static const List<_NavItem> _navItems = [
    _NavItem(label: '홈', icon: Icons.home),
    _NavItem(label: '검색', icon: Icons.search),
    _NavItem(label: '탐색', icon: Icons.explore),
    _NavItem(label: '설정', icon: Icons.settings),
  ];

  @override
  void initState() {
    super.initState();
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
      // Pause explore audio when leaving the explore tab.
      ExploreScreenState.pauseActiveAudio();
      ExploreScreenState.setActivePlayback(false);
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
            // Progress bar only shows on explore tab. Draggable to seek.
            ValueListenableBuilder<double>(
              valueListenable: ExploreScreenState.progressListenable(),
              builder: (context, value, _) {
                if (_currentIndex != 2) return const SizedBox.shrink();
                final isExploreRoot =
                    !(_navigatorKeys[2].currentState?.canPop() ?? false);
                if (!isExploreRoot) return const SizedBox.shrink();
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
                    onChanged: (v) {
                      ExploreScreenState.updateTempProgress(v);
                    },
                    onChangeStart: (_) => ExploreScreenState.beginUserSeek(),
                    onChangeEnd: (v) => ExploreScreenState.endUserSeek(v),
                  ),
                );
              },
            ),
            SafeArea(
              top: false,
              bottom: false,
              child: MediaQuery.removePadding(
                context: context,
                removeBottom: true,
                child: Container(
                  color: AppColors.gray1,
                  child: Padding(
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

class _PlaceholderScreen extends StatelessWidget {
  const _PlaceholderScreen({required this.label});

  final String label;

  @override
  Widget build(BuildContext context) {
    return Container(
      color: AppColors.hearitBlack,
      alignment: Alignment.center,
      child: Text(
        '$label 화면 준비 중',
        style: Theme.of(context).textTheme.titleMedium?.copyWith(
          color: AppColors.gray4.withOpacity(0.7),
          fontWeight: FontWeight.w600,
        ),
      ),
    );
  }
}
