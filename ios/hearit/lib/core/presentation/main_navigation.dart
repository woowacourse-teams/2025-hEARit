import 'package:flutter/material.dart';

import '../../features/home/home_screen.dart';
import '../../features/search/search_screen.dart';

class MainNavigation extends StatefulWidget {
  const MainNavigation({super.key});

  @override
  State<MainNavigation> createState() => _MainNavigationState();
}

class _MainNavigationState extends State<MainNavigation> {
  int _currentIndex = 0;

  late final List<Widget> _pages = [
    HomeScreen(onExploreTap: () => _onItemTapped(2)),
    SearchScreen(onBackToHome: () => _onItemTapped(0)),
    const _PlaceholderScreen(label: '탐색'),
    const _PlaceholderScreen(label: '라이브러리'),
  ];

  static const List<_NavItem> _navItems = [
    _NavItem(label: '홈', icon: Icons.home),
    _NavItem(label: '검색', icon: Icons.search),
    _NavItem(label: '탐색', icon: Icons.compass_calibration_rounded),
    _NavItem(label: '라이브러리', icon: Icons.library_books),
  ];

  void _onItemTapped(int index) {
    setState(() {
      _currentIndex = index;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF1F1F1F),
      body: IndexedStack(index: _currentIndex, children: _pages),
      bottomNavigationBar: Container(
        color: const Color(0xFF2C2C2C),
        padding: const EdgeInsets.only(top: 4),
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 20),
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
                      color: const Color(0xFFBFBFBF),
                      iconSize: 38,
                    ),
                    activeIcon: _NavVisual(
                      icon: item.icon,
                      label: item.label,
                      color: const Color(0xFFA86BFF),
                      iconSize: 38,
                    ),
                    label: item.label,
                  ),
                )
                .toList(),
          ),
        ),
      ),
    );
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
        const SizedBox(height: 5),
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
      color: const Color(0xFF1F1F1F),
      alignment: Alignment.center,
      child: Text(
        '$label 화면 준비 중',
        style: Theme.of(context).textTheme.titleMedium?.copyWith(
          color: Colors.white70,
          fontWeight: FontWeight.w600,
        ),
      ),
    );
  }
}
