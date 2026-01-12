import 'package:flutter/material.dart';
import 'package:hearit/core/analytics/analytics_event_names.dart';
import 'package:hearit/core/analytics/analytics_param_keys.dart';
import 'package:hearit/core/analytics/analytics_provider.dart';
import 'package:hearit/core/theme/app_colors.dart';

import '../detail/hearit_detail.dart';
import '../detail/hearit_detail_screen.dart';
import 'category_hearit_screen.dart';
import 'search_models.dart';
import 'search_viewmodel.dart';
import 'widgets/search_category_grid.dart';
import 'widgets/search_result_card.dart';

class SearchScreen extends StatefulWidget {
  const SearchScreen({super.key, this.onBackToHome});

  final VoidCallback? onBackToHome;

  @override
  State<SearchScreen> createState() => _SearchScreenState();
}

class _SearchScreenState extends State<SearchScreen> {
  late final SearchViewModel _viewModel;

  @override
  void initState() {
    super.initState();
    _viewModel = SearchViewModel()..addListener(_onViewModelUpdated);
    WidgetsBinding.instance.addPostFrameCallback((_) => _logScreenView());
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

  void _logScreenView() {
    AnalyticsProvider.logger.logEvent(
      'screen_view',
      params: {
        AnalyticsParamKeys.screenName: AnalyticsParamKeys.screenNameSearch,
        AnalyticsParamKeys.screenClass: 'SearchScreen',
      },
    );
  }

  void _submitQuery() {
    final term = _viewModel.query.trim();
    if (term.isNotEmpty) {
      AnalyticsProvider.logger.logEvent(
        AnalyticsEventNames.searchKeywordEntered,
        params: {AnalyticsParamKeys.searchKeyword: term},
      );
    }
    _viewModel.submitQuery();
  }

  void _handleBackButton() {
    if (_viewModel.canGoBack) {
      _viewModel.stepBack();
    } else {
      _viewModel.clearQuery();
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Scaffold(
      backgroundColor: AppColors.hearitBlack,

      // iOS 상단 노치 + status bar 침범 방지
      body: Container(
        color: AppColors.hearitBlack,
        padding: EdgeInsets.only(top: MediaQuery.of(context).padding.top),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // ----------------------------------------
            // [1] 상단 고정 UI 영역
            // ----------------------------------------
            Container(
              width: double.infinity,
              padding: const EdgeInsets.only(
                left: 8,
                right: 8,
                top: 0,
                bottom: 20,
              ),
              color: AppColors.hearitBlack,
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                mainAxisSize: MainAxisSize.min,
                children: [
                  // 검색 전: 왼쪽 정렬, 검색 후: 뒤로가기 버튼 + 중앙 정렬
                  _viewModel.hasSearched
                      ? Row(
                          children: [
                            IconButton(
                              padding: EdgeInsets.zero,
                              constraints: const BoxConstraints(
                                minWidth: 40,
                                minHeight: 40,
                              ),
                              icon: const Icon(
                                Icons.chevron_left,
                                color: Colors.white,
                                size: 32,
                              ),
                              onPressed: _handleBackButton,
                            ),
                            Expanded(
                              child: Center(
                                child: Text(
                                  '검색',
                                  style: theme.textTheme.titleMedium?.copyWith(
                                    color: AppColors.gray4,
                                    fontWeight: FontWeight.w800,
                                    fontSize: 20,
                                  ),
                                ),
                              ),
                            ),
                            const SizedBox(width: 40),
                          ],
                        )
                      : Padding(
                          padding: const EdgeInsets.only(left: 12),
                          child: Text(
                            '검색',
                            style: theme.textTheme.titleMedium?.copyWith(
                              color: AppColors.gray4,
                              fontWeight: FontWeight.w800,
                              fontSize: 28,
                            ),
                          ),
                        ),
                  Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 12),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const SizedBox(height: 4),

                        // 검색 입력창
                        TextField(
                          controller: _viewModel.searchController,
                          onSubmitted: (_) => _submitQuery(),
                          textInputAction: TextInputAction.search,
                          style: theme.textTheme.bodyLarge?.copyWith(
                            color: AppColors.gray4,
                            fontSize: 16,
                            fontWeight: FontWeight.w500,
                          ),
                          cursorColor: AppColors.gray4,
                          decoration: InputDecoration(
                            isDense: true,
                            contentPadding: const EdgeInsets.only(
                              top: 14,
                              bottom: 0,
                            ),
                            hintText: '검색어를 입력해주세요.',
                            hintStyle: theme.textTheme.bodyMedium?.copyWith(
                              color: AppColors.gray2,
                              fontSize: 16,
                              fontWeight: FontWeight.w500,
                            ),
                            suffixIcon: Row(
                              mainAxisSize: MainAxisSize.min,
                              children: [
                                if (_viewModel.hasQuery)
                                  IconButton(
                                    padding: EdgeInsets.zero,
                                    constraints: const BoxConstraints(),
                                    icon: const Icon(Icons.close, size: 22),
                                    color: Colors.white70,
                                    onPressed: _viewModel.clearQuery,
                                    splashRadius: 18,
                                  ),
                                if (!_viewModel.hasQuery)
                                  const SizedBox(width: 18, height: 22),
                                const SizedBox(width: 8),
                                IconButton(
                                  padding: EdgeInsets.zero,
                                  constraints: const BoxConstraints(),
                                  icon: Icon(
                                    Icons.search,
                                    color: _viewModel.hasQuery
                                        ? AppColors.gray4
                                        : Colors.white70,
                                    size: 30,
                                  ),
                                  onPressed: _submitQuery,
                                  splashRadius: 20,
                                ),
                              ],
                            ),
                            enabledBorder: const UnderlineInputBorder(
                              borderSide: BorderSide(color: Color(0xFF8E8E93)),
                            ),
                            focusedBorder: const UnderlineInputBorder(
                              borderSide: BorderSide(color: Colors.white70),
                            ),
                          ),
                        ),

                        if (_viewModel.isLoading) ...[
                          const SizedBox(height: 12),
                          const LinearProgressIndicator(
                            minHeight: 2,
                            color: Color(0xFFA86BFF),
                            backgroundColor: Color(0xFF3B3B46),
                          ),
                        ],

                        if (_viewModel.error != null) ...[
                          const SizedBox(height: 12),
                          Text(
                            _viewModel.error!,
                            style: theme.textTheme.bodyMedium?.copyWith(
                              color: Colors.redAccent,
                            ),
                          ),
                        ],

                        const SizedBox(height: 16),

                        if (_viewModel.hasSearched)
                          Text(
                            '검색된 히어릿 목록',
                            style: theme.textTheme.titleMedium?.copyWith(
                              color: AppColors.gray4,
                              fontWeight: FontWeight.bold,
                              fontSize: 18,
                            ),
                          ),
                      ],
                    ),
                  ),
                ],
              ),
            ),

            // ----------------------------------------
            // [2] 아래 스크롤 영역
            // ----------------------------------------
            Expanded(
              child: Container(
                color: AppColors.hearitBlack, // 스크롤 영역 전체 배경 고정
                child: !_viewModel.hasSearched
                    ? SingleChildScrollView(
                        padding: EdgeInsets.only(
                          left: 20,
                          right: 20,
                          top: 0,
                          bottom: 20 + MediaQuery.of(context).padding.bottom,
                        ),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              '카테고리',
                              style: theme.textTheme.titleMedium?.copyWith(
                                color: AppColors.gray4,
                                fontWeight: FontWeight.w600,
                                fontSize: 20,
                                height: 1.0,
                              ),
                              textHeightBehavior: const TextHeightBehavior(
                                applyHeightToFirstAscent: false,
                                applyHeightToLastDescent: false,
                              ),
                            ),
                            const SizedBox(height: 20),
                            if (_viewModel.categoriesLoading)
                              const LinearProgressIndicator(
                                minHeight: 2,
                                color: Color(0xFFA86BFF),
                                backgroundColor: Color(0xFF3B3B46),
                              )
                            else if (_viewModel.categoriesError != null)
                              Text(
                                _viewModel.categoriesError!,
                                style: theme.textTheme.bodyMedium?.copyWith(
                                  color: Colors.redAccent,
                                ),
                              )
                            else
                              MediaQuery.removePadding(
                                context: context,
                                removeTop: true,
                                child: Padding(
                                  padding: const EdgeInsets.only(bottom: 20),
                                  child: SearchCategoryGrid(
                                    categories: _viewModel.categories,
                                    onTap: (category) {
                                      AnalyticsProvider.logger.logEvent(
                                        AnalyticsEventNames
                                            .searchCategorySelected,
                                        params: {
                                          AnalyticsParamKeys.categoryName:
                                              category.name,
                                        },
                                      );
                                      Navigator.of(context).push(
                                        MaterialPageRoute(
                                          builder: (_) => CategoryHearitScreen(
                                            category: category,
                                          ),
                                        ),
                                      );
                                    },
                                  ),
                                ),
                              ),
                          ],
                        ),
                      )
                    : _viewModel.results.isEmpty && !_viewModel.isLoading
                    ? Center(
                        child: Text(
                          '검색 결과가 없습니다.',
                          style: theme.textTheme.bodyMedium?.copyWith(
                            color: Colors.white70,
                          ),
                        ),
                      )
                    : MediaQuery.removePadding(
                        context: context,
                        removeTop: true, // 상단 고정 UI 위로 스크롤 방지
                        child: ListView.separated(
                          padding: EdgeInsets.only(
                            left: 20,
                            right: 20,
                            bottom:
                                20 + 60 + MediaQuery.of(context).padding.bottom,
                          ),
                          physics: const ClampingScrollPhysics(),
                          itemCount: _viewModel.results.length,
                          separatorBuilder: (_, __) =>
                              const SizedBox(height: 12),
                          itemBuilder: (context, index) {
                            final data = _viewModel.results[index];
                            return SearchResultCard(
                              data: data,
                              onTap: () => _openDetail(data),
                            );
                          },
                        ),
                      ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  void _openDetail(SearchHearit data) {
    AnalyticsProvider.logger.logEvent(
      AnalyticsEventNames.searchHearitSelected,
      params: {AnalyticsParamKeys.itemId: data.id.toString()},
    );
    final stub = HearitDetail.fromSummaryStub(
      id: data.id,
      title: data.title,
      categoryName: '검색',
      accentColor: const Color(0xFFA86BFF),
      createdAt: DateTime.now(),
    );
    Navigator.of(
      context,
    ).push(MaterialPageRoute(builder: (_) => HearitDetailScreen(detail: stub)));
  }
}
