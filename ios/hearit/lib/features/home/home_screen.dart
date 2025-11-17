import 'package:flutter/material.dart';

import '../detail/hearit_detail.dart';
import '../detail/hearit_detail_screen.dart';
import 'home_models.dart';
import 'home_viewmodel.dart';
import 'widgets/category_section.dart';
import 'widgets/explore_shortcut_card.dart';
import 'widgets/home_header.dart';
import 'widgets/listening_section.dart';
import 'widgets/recommend_widgets.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key, this.onExploreTap});

  final VoidCallback? onExploreTap;

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  late final HomeViewModel _viewModel;
  late final PageController _pageController;
  double _page = 0;
  static const bool _showListeningAndBookmarks = false;
  static const bool _showRecentlyAdded = true;

  @override
  void initState() {
    super.initState();
    _viewModel = HomeViewModel()..addListener(_onViewModelUpdated);
    _viewModel.loadHome();
    _pageController = PageController(viewportFraction: 0.6, initialPage: 2);
    _page = _pageController.initialPage.toDouble();
    _pageController.addListener(() {
      setState(() {
        _page = _pageController.page ?? 0;
      });
    });
  }

  @override
  void dispose() {
    _viewModel.removeListener(_onViewModelUpdated);
    _viewModel.dispose();
    _pageController.dispose();
    super.dispose();
  }

  void _onViewModelUpdated() {
    if (mounted) setState(() {});
  }

  List<Widget> _buildCards(double pageWidth) {
    final List<Map<String, dynamic>> cardDataList = [];
    for (
      int index = 0;
      index < _viewModel.todayRecommendedHearits.length;
      index++
    ) {
      final hearit = _viewModel.todayRecommendedHearits[index];
      final pageOffset = _page - index;
      final distance = pageOffset.abs();
      final isFocused = distance < 0.5;

      final double opacity = isFocused ? 1.0 : 0.6;
      final double scale = (1 - (distance * 0.12)).clamp(0.88, 1.0);
      final double horizontalPosition = (index - _page) * pageWidth;

      final cardWidget = Align(
        alignment: Alignment.center,
        child: Transform.translate(
          offset: Offset(horizontalPosition, 0.0),
          child: Transform.scale(
            scale: scale,
            child: Opacity(
              opacity: opacity,
              child: SizedBox(
                width: 300,
                height: 340,
                child: RecommendCard(
                  data: hearit,
                  onTap: () => _openHearitDetail(
                    _viewModel.toHearitDetailFromRecommend(hearit),
                  ),
                ),
              ),
            ),
          ),
        ),
      );

      cardDataList.add({'widget': cardWidget, 'distance': distance});
    }

    cardDataList.sort(
      (a, b) => (b['distance'] as double).compareTo(a['distance'] as double),
    );

    return cardDataList.map((data) => data['widget'] as Widget).toList();
  }

  void _openHearitDetail(HearitDetail detail) {
    Navigator.of(context).push(
      MaterialPageRoute(builder: (_) => HearitDetailScreen(detail: detail)),
    );
  }

  @override
  Widget build(BuildContext context) {
    final double viewPortWidth = MediaQuery.of(context).size.width - 40;
    final double pageWidth = viewPortWidth * _pageController.viewportFraction;

    return Scaffold(
      backgroundColor: const Color(0xFF1F1F1F),
      body: SafeArea(
        bottom: false,
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 20),
          child: SingleChildScrollView(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const SizedBox(height: 12),
                const HomeHeader(),
                if (_viewModel.isLoading) ...[
                  const SizedBox(height: 12),
                  const LinearProgressIndicator(
                    minHeight: 2,
                    color: Color(0xFFA86BFF),
                    backgroundColor: Color(0xFF3B3B46),
                  ),
                ],
                if (_viewModel.error != null && !_viewModel.isLoading) ...[
                  const SizedBox(height: 12),
                  Text(
                    '데이터를 불러오지 못했습니다. 다시 시도해 주세요.',
                    style: Theme.of(
                      context,
                    ).textTheme.bodyMedium?.copyWith(color: Colors.redAccent),
                  ),
                ],
                const SizedBox(height: 24),
                Text(
                  '오늘 추천하는 팟캐스트',
                  style: Theme.of(context).textTheme.titleMedium?.copyWith(
                    fontWeight: FontWeight.bold,
                    color: Colors.white,
                    fontSize: 22,
                  ),
                ),
                SizedBox(
                  height: 380,
                  child: Stack(
                    clipBehavior: Clip.none,
                    children: [
                      ..._buildCards(pageWidth),
                      PageView.builder(
                        controller: _pageController,
                        clipBehavior: Clip.none,
                        itemCount: _viewModel.todayRecommendedHearits.length,
                        itemBuilder: (context, index) {
                          final hearit = _viewModel.todayRecommendedHearits[index];
                          final detail =
                              _viewModel.toHearitDetailFromRecommend(hearit);
                          return GestureDetector(
                            behavior: HitTestBehavior.translucent,
                            onTap: () => _openHearitDetail(detail),
                            child: const SizedBox.expand(),
                          );
                        },
                      ),
                      Positioned(
                        bottom: -12,
                        left: 0,
                        right: 0,
                        child: RecommendIndicator(
                          currentPage: _page,
                          itemCount: _viewModel.todayRecommendedHearits.length,
                        ),
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 36),
                if (_showListeningAndBookmarks) ...[
                  ListeningSection(
                    title: 'hEARit님이 듣고 있는 팟캐스트',
                    items: _viewModel.listeningNowHearits,
                    onTap: (data) => _openHearitDetail(
                      _viewModel.toHearitDetailFromListening(data),
                    ),
                  ),
                  // const SizedBox(height: 10),
                  ListeningSection(
                    title: '북마크한 팟캐스트를 들어보세요',
                    items: _viewModel.bookmarkedHearits,
                    showChevron: true,
                    onTap: (data) => _openHearitDetail(
                      _viewModel.toHearitDetailFromListening(data),
                    ),
                  ),
                  // const SizedBox(height: 24),
                ],
                if (_showRecentlyAdded) ...[
                  ListeningSection(
                    title: '최근 추가된 팟캐스트',
                    items: _viewModel.recentlyAddedHearits,
                    onTap: (data) => _openHearitDetail(
                      _viewModel.toHearitDetailFromListening(data),
                    ),
                  ),
                  // const SizedBox(height: 24),
                ],
                ExploreShortcutCard(onTap: widget.onExploreTap),
                const SizedBox(height: 32),
                CategorySection(
                  sections: _viewModel.curatedCategoryHearits,
                  onHearitTap: (section, podcast) => _openHearitDetail(
                    _viewModel.toHearitDetailFromCategory(section, podcast),
                  ),
                ),
                const SizedBox(height: 40),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
