import 'package:flutter/material.dart';
import 'package:hearit/core/theme/app_colors.dart';

import '../detail/hearit_detail.dart';
import '../detail/hearit_detail_screen.dart';
import 'search_models.dart';
import 'search_repository.dart';
import 'widgets/search_result_card.dart';

class CategoryHearitScreen extends StatefulWidget {
  const CategoryHearitScreen({super.key, required this.category});

  final SearchCategory category;

  @override
  State<CategoryHearitScreen> createState() => _CategoryHearitScreenState();
}

class _CategoryHearitScreenState extends State<CategoryHearitScreen> {
  final SearchRepository _repository = SearchRepository();
  late Future<List<SearchHearit>> _future;

  @override
  void initState() {
    super.initState();
    _future = _repository.fetchHearitsByCategory(
      categoryId: widget.category.id,
    );
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final top = MediaQuery.of(context).padding.top;
    final Color accent = widget.category.color;

    return Scaffold(
      backgroundColor: AppColors.hearitBlack,
      body: Column(
        children: [
          Container(
            width: double.infinity,
            padding: EdgeInsets.only(
              left: 12,
              right: 12,
              top: top + 12,
              bottom: 62,
            ),
            decoration: BoxDecoration(
              gradient: LinearGradient(
                begin: Alignment.topCenter,
                end: Alignment.bottomCenter,
                colors: [accent, accent.withOpacity(0)],
              ),
            ),
            child: Row(
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
                  onPressed: () => Navigator.of(context).pop(),
                ),
                Expanded(
                  child: Center(
                    child: Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Text(
                          widget.category.name,
                          style: theme.textTheme.titleMedium?.copyWith(
                            color: Colors.white,
                            fontWeight: FontWeight.bold,
                            fontSize: 28,
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
                const SizedBox(width: 40),
              ],
            ),
          ),
          Expanded(
            child: FutureBuilder<List<SearchHearit>>(
              future: _future,
              builder: (context, snapshot) {
                if (snapshot.connectionState == ConnectionState.waiting) {
                  return const Center(
                    child: CircularProgressIndicator(color: Color(0xFFA86BFF)),
                  );
                }
                if (snapshot.hasError) {
                  return Center(
                    child: Text(
                      '카테고리 히어릿을 불러오지 못했습니다.',
                      style: theme.textTheme.bodyMedium?.copyWith(
                        color: Colors.redAccent,
                      ),
                    ),
                  );
                }
                final data = snapshot.data ?? [];
                if (data.isEmpty) {
                  return Center(
                    child: Text(
                      '히어릿이 없습니다.',
                      style: theme.textTheme.bodyMedium?.copyWith(
                        color: Colors.white70,
                      ),
                    ),
                  );
                }
                return ListView.separated(
                  padding: const EdgeInsets.symmetric(
                    horizontal: 16,
                    vertical: 24,
                  ),
                  itemCount: data.length,
                  separatorBuilder: (_, __) => const SizedBox(height: 12),
                  itemBuilder: (context, index) {
                    return SearchResultCard(
                      data: data[index],
                      onTap: () => _openDetail(data[index]),
                      progressColor: widget.category.color,
                    );
                  },
                );
              },
            ),
          ),
        ],
      ),
    );
  }

  void _openDetail(SearchHearit data) {
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
