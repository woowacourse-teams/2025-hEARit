import 'package:flutter/material.dart';

import 'search_viewmodel.dart';
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
    final theme = Theme.of(context);

    return Scaffold(
      backgroundColor: const Color(0xFF1F1F1F),

      // iOS 상단 노치 + status bar 침범 방지
      body: Container(
        color: const Color(0xFF1F1F1F),
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
                top: 20,
                bottom: 20,
              ),
              color: const Color(0xFF1F1F1F),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                mainAxisSize: MainAxisSize.min,
                children: [
                  Row(
                    children: [
                      Transform.translate(
                        offset: const Offset(-8, 0),
                        child: IconButton(
                          padding: const EdgeInsets.only(left: 4),
                          constraints: const BoxConstraints(
                            minWidth: 36,
                            minHeight: 36,
                          ),
                          icon: const Icon(
                            Icons.chevron_left,
                            color: Colors.white,
                            size: 35,
                          ),
                          onPressed: () {
                            if (_viewModel.canGoBack) {
                              _viewModel.stepBack();
                              return;
                            } else if (_viewModel.hasSearched ||
                                _viewModel.hasQuery ||
                                _viewModel.isLoading) {
                              _viewModel.clearQuery();
                              return;
                            }
                            if (widget.onBackToHome != null) {
                              widget.onBackToHome!();
                              return;
                            }
                            Navigator.of(context).maybePop();
                          },
                        ),
                      ),
                      Expanded(
                        child: Center(
                          child: Text(
                            '검색',
                            style: theme.textTheme.titleMedium?.copyWith(
                              color: Colors.white,
                              fontWeight: FontWeight.bold,
                              fontSize: 18,
                            ),
                          ),
                        ),
                      ),
                      const SizedBox(width: 48), // balance back button space
                    ],
                  ),
                  Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 12),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const SizedBox(height: 16),

                        // 검색 입력창
                        TextField(
                          controller: _viewModel.searchController,
                          onSubmitted: (_) => _viewModel.submitQuery(),
                          textInputAction: TextInputAction.search,
                          style: theme.textTheme.bodyLarge?.copyWith(
                            color: Colors.white,
                            fontSize: 16,
                          ),
                          cursorColor: Colors.white,
                          decoration: InputDecoration(
                            isDense: true,
                            hintText: '검색어를 입력해주세요.',
                            hintStyle: theme.textTheme.bodyMedium?.copyWith(
                              color: Colors.white70,
                              fontSize: 16,
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
                                  const SizedBox(width: 22, height: 22),
                                const SizedBox(width: 8),
                                IconButton(
                                  padding: EdgeInsets.zero,
                                  constraints: const BoxConstraints(),
                                  icon: Icon(
                                    Icons.search,
                                    color: _viewModel.hasQuery
                                        ? Colors.white
                                        : Colors.white70,
                                    size: 30,
                                  ),
                                  onPressed: _viewModel.submitQuery,
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
                              color: Colors.white,
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
                color: const Color(0xFF1F1F1F), // 스크롤 영역 전체 배경 고정
                child: !_viewModel.hasSearched
                    ? const SizedBox.shrink()
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
                          padding: const EdgeInsets.only(
                            left: 20,
                            right: 20,
                            bottom: 20,
                          ),
                          physics: const ClampingScrollPhysics(),
                          itemCount: _viewModel.results.length,
                          separatorBuilder: (_, __) =>
                              const SizedBox(height: 12),
                          itemBuilder: (context, index) {
                            final data = _viewModel.results[index];
                            return SearchResultCard(data: data);
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
}
