import 'package:flutter/material.dart';
import 'package:hearit/core/analytics/analytics_event_names.dart';
import 'package:hearit/core/analytics/analytics_param_keys.dart';
import 'package:hearit/core/analytics/analytics_provider.dart';
import 'package:hearit/core/theme/app_colors.dart';

import '../detail/hearit_detail.dart';
import '../detail/hearit_detail_screen.dart';
import 'home_models.dart';
import 'home_viewmodel.dart';
import 'widgets/category_section.dart';
import 'widgets/explore_shortcut_card.dart';
import 'widgets/home_header.dart';
import 'widgets/listening_section.dart';
import 'widgets/recommend_widgets.dart';
import '../search/category_hearit_screen.dart';
import '../search/search_models.dart';

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
  static const double _maxOverscrollExtent = 20;

  @override
  void initState() {
    super.initState();
    _viewModel = HomeViewModel()..addListener(_onViewModelUpdated);
    _viewModel.loadHome();
    WidgetsBinding.instance.addPostFrameCallback((_) => _logScreenView());
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
                  onTap: () => _onRecommendTap(hearit),
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

  void _openCategory(CategorySectionData section) {
    Navigator.of(context).push(
      MaterialPageRoute(
        builder: (_) => CategoryHearitScreen(
          category: SearchCategory(
            id: section.categoryId,
            name: section.categoryName,
            color: section.accentColor,
          ),
        ),
      ),
    );
  }

  void _logScreenView() {
    AnalyticsProvider.logger.logEvent(
      'screen_view',
      params: {
        AnalyticsParamKeys.screenName: AnalyticsParamKeys.screenNameHome,
        AnalyticsParamKeys.screenClass: 'HomeScreen',
      },
    );
  }

  void _handleExploreTap() {
    AnalyticsProvider.logger.logEvent(AnalyticsEventNames.homeExploreSelected);
    widget.onExploreTap?.call();
  }

  void _onRecommendTap(RecommendCardData data) {
    AnalyticsProvider.logger.logEvent(
      AnalyticsEventNames.homeRecommendSelected,
      params: {AnalyticsParamKeys.itemId: data.id.toString()},
    );
    _openHearitDetail(_viewModel.toHearitDetailFromRecommend(data));
  }

  void _onRecentUploadTap(ListeningCardData data) {
    AnalyticsProvider.logger.logEvent(
      AnalyticsEventNames.homeRecentUploadSelected,
      params: {AnalyticsParamKeys.itemId: data.id.toString()},
    );
    _openHearitDetail(_viewModel.toHearitDetailFromListening(data));
  }

  void _onPlayingHistoryTap(ListeningCardData data) {
    AnalyticsProvider.logger.logEvent(
      AnalyticsEventNames.homePlayingHistorySelected,
      params: {AnalyticsParamKeys.itemId: data.id.toString()},
    );
    _openHearitDetail(_viewModel.toHearitDetailFromListening(data));
  }

  void _onPlayingBookmarkTap(ListeningCardData data) {
    AnalyticsProvider.logger.logEvent(
      AnalyticsEventNames.homePlayingBookmarkSelected,
      params: {AnalyticsParamKeys.itemId: data.id.toString()},
    );
    _openHearitDetail(_viewModel.toHearitDetailFromListening(data));
  }

  void _onCategoryTap(CategorySectionData section) {
    AnalyticsProvider.logger.logEvent(
      AnalyticsEventNames.homeRecommendationCategorySelected,
      params: {
        AnalyticsParamKeys.itemId: section.categoryId.toString(),
        AnalyticsParamKeys.categoryName: section.categoryName,
      },
    );
    _openCategory(section);
  }

  void _onCategoryHearitTap(
    CategorySectionData section,
    CategoryPodcastData podcast,
  ) {
    AnalyticsProvider.logger.logEvent(
      AnalyticsEventNames.homeRecommendationCategoryHearitSelected,
      params: {AnalyticsParamKeys.itemId: podcast.id.toString()},
    );
    _openHearitDetail(_viewModel.toHearitDetailFromCategory(section, podcast));
  }

  Future<void> _refreshHome() async {
    await _viewModel.loadHome();
    if (_pageController.hasClients) {
      final int targetPage =
          _viewModel.todayRecommendedHearits.length >
              _pageController.initialPage
          ? _pageController.initialPage
          : 0;
      _pageController.jumpToPage(targetPage);
      setState(() {
        _page = targetPage.toDouble();
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final double viewPortWidth = MediaQuery.of(context).size.width - 40;
    final double pageWidth = viewPortWidth * _pageController.viewportFraction;

    return Scaffold(
      backgroundColor: AppColors.hearitBlack,
      body: SafeArea(
        bottom: false,
        child: RefreshIndicator(
          color: const Color(0xFFA86BFF),
          backgroundColor: AppColors.hearitBlack,
          onRefresh: _refreshHome,
          child: SingleChildScrollView(
            physics: const _LimitedOverscrollPhysics(),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const SizedBox(height: 12),
                const Padding(
                  padding: EdgeInsets.symmetric(horizontal: 20),
                  child: HomeHeader(),
                ),
                if (_viewModel.isLoading) ...[
                  const SizedBox(height: 12),
                  const Padding(
                    padding: EdgeInsets.symmetric(horizontal: 20),
                    child: LinearProgressIndicator(
                      minHeight: 2,
                      color: Color(0xFFA86BFF),
                      backgroundColor: Color(0xFF3B3B46),
                    ),
                  ),
                ],
                if (_viewModel.error != null && !_viewModel.isLoading) ...[
                  const SizedBox(height: 12),
                  Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 20),
                    child: Text(
                      '데이터를 불러오지 못했습니다. 다시 시도해 주세요.',
                      style: Theme.of(
                        context,
                      ).textTheme.bodyMedium?.copyWith(color: Colors.redAccent),
                    ),
                  ),
                ],
                Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 20),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const SizedBox(height: 24),
                      Text(
                        '오늘 추천하는 팟캐스트',
                        style: Theme.of(context).textTheme.titleMedium
                            ?.copyWith(
                              fontWeight: FontWeight.bold,
                              color: AppColors.gray4,
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
                              itemCount:
                                  _viewModel.todayRecommendedHearits.length,
                              itemBuilder: (context, index) {
                                final hearit =
                                    _viewModel.todayRecommendedHearits[index];
                                return GestureDetector(
                                  behavior: HitTestBehavior.translucent,
                                  onTap: () => _onRecommendTap(hearit),
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
                                itemCount:
                                    _viewModel.todayRecommendedHearits.length,
                              ),
                            ),
                          ],
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
                    onTap: _onPlayingHistoryTap,
                  ),
                  ListeningSection(
                    title: '북마크한 팟캐스트를 들어보세요',
                    items: _viewModel.bookmarkedHearits,
                    showChevron: true,
                    onTap: _onPlayingBookmarkTap,
                  ),
                ],
                if (_showRecentlyAdded) ...[
                  ListeningSection(
                    title: '최근 추가된 팟캐스트',
                    items: _viewModel.recentlyAddedHearits,
                    onTap: _onRecentUploadTap,
                  ),
                ],
                Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 20),
                  child: ExploreShortcutCard(onTap: _handleExploreTap),
                ),
                const SizedBox(height: 32),
                CategorySection(
                  sections: _viewModel.curatedCategoryHearits,
                  onCategoryTap: _onCategoryTap,
                  onHearitTap: _onCategoryHearitTap,
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

class _LimitedOverscrollPhysics extends AlwaysScrollableScrollPhysics {
  const _LimitedOverscrollPhysics({
    super.parent,
    this.maxOverscroll = _HomeScreenState._maxOverscrollExtent,
  });

  final double maxOverscroll;

  @override
  _LimitedOverscrollPhysics applyTo(ScrollPhysics? ancestor) {
    return _LimitedOverscrollPhysics(
      parent: buildParent(ancestor),
      maxOverscroll: maxOverscroll,
    );
  }

  @override
  double applyBoundaryConditions(ScrollMetrics position, double value) {
    final double min = position.minScrollExtent;
    final double max = position.maxScrollExtent;

    if (value < min - maxOverscroll) {
      return value - (min - maxOverscroll);
    }
    if (value > max + maxOverscroll) {
      return value - (max + maxOverscroll);
    }
    return 0;
  }
}
