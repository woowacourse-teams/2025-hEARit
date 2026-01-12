import 'package:flutter/material.dart';
import 'package:hearit/core/theme/app_colors.dart';
import 'package:provider/provider.dart';

import '../../core/analytics/analytics_event_names.dart';
import '../../core/analytics/analytics_param_keys.dart';
import '../../core/analytics/analytics_provider.dart';
import '../../core/audio/hearit_player_controller.dart';
import '../auth/login_screen.dart';
import '../detail/hearit_detail.dart' as detail;
import '../detail/hearit_detail_screen.dart';
import 'library_models.dart';
import 'library_viewmodel.dart';
import 'playlist_models.dart';
import 'widgets/bookmark_delete_dialog.dart';
import 'widgets/bookmark_section_header.dart';
import 'widgets/bookmarked_hearit_card.dart';
import 'widgets/library_header.dart';

/// 라이브러리 메인 화면
/// 사용자가 북마크한 히어릿 목록 표시
class LibraryScreen extends StatefulWidget {
  const LibraryScreen({super.key});

  @override
  State<LibraryScreen> createState() => _LibraryScreenState();
}

class _LibraryScreenState extends State<LibraryScreen> {
  late final LibraryViewModel _viewModel;
  final ScrollController _scrollController = ScrollController();

  @override
  void initState() {
    super.initState();
    _viewModel = LibraryViewModel()..addListener(_onViewModelChanged);
    _viewModel.loadInitialData();
    _scrollController.addListener(_onScroll);

    // Analytics: 화면 진입
    WidgetsBinding.instance.addPostFrameCallback((_) {
      AnalyticsProvider.logger.logEvent(
        AnalyticsEventNames.libraryScreenViewed,
        params: {
          AnalyticsParamKeys.screenName: AnalyticsParamKeys.screenNameLibrary,
        },
      );
    });
  }

  @override
  void dispose() {
    _viewModel.removeListener(_onViewModelChanged);
    _viewModel.dispose();
    _scrollController.dispose();
    super.dispose();
  }

  void _onViewModelChanged() {
    if (mounted) {
      setState(() {});

      // 무한 스크롤 중 인증 에러 처리
      if (_viewModel.loadMoreAuthError != null) {
        WidgetsBinding.instance.addPostFrameCallback((_) {
          if (mounted) {
            ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(
                content: Text(_viewModel.loadMoreAuthError!),
                backgroundColor: AppColors.gray2,
                action: SnackBarAction(
                  label: '로그인',
                  textColor: AppColors.hearitPurple3,
                  onPressed: () {
                    Navigator.of(
                      context,
                      rootNavigator: true,
                    ).pushAndRemoveUntil(
                      MaterialPageRoute(builder: (_) => const LoginScreen()),
                      (route) => false,
                    );
                  },
                ),
                duration: const Duration(seconds: 3),
              ),
            );
            _viewModel.clearLoadMoreAuthError();
          }
        });
      }
    }
  }

  void _onScroll() {
    final position = _scrollController.position;

    // extentAfter: 현재 위치에서 끝까지의 남은 거리
    // extentAfter < 200: 하단에서 200px 이내
    // extentAfter > 0: 실제로 스크롤 가능한 컨텐츠가 있음 (maxScrollExtent > pixels)
    if (position.extentAfter < 200 && position.extentAfter > 0) {
      _viewModel.loadMoreBookmarks();
    }
  }

  Future<void> _handleRefresh() async {
    await _viewModel.refreshBookmarks();
  }

  void _handleCardTap(BookmarkedHearit hearit) {
    // Analytics: 히어릿 선택
    AnalyticsProvider.logger.logEvent(
      AnalyticsEventNames.libraryHearitSelected,
      params: {
        AnalyticsParamKeys.itemId: hearit.hearitId,
        AnalyticsParamKeys.itemName: hearit.title,
        AnalyticsParamKeys.categoryName: hearit.category.name,
      },
    );

    final detail = _convertToHearitDetail(hearit);
    Navigator.of(context).push(
      MaterialPageRoute(
        builder: (_) => HearitDetailScreen(detail: detail),
        settings: const RouteSettings(name: '/detail'),
      ),
    );
  }

  Future<void> _handleMenuTap(BookmarkedHearit hearit) async {
    // BottomSheet 표시
    final action = await BookmarkMenuSheet.show(
      context,
      hearitTitle: hearit.title,
    );

    if (action == BookmarkMenuAction.delete && mounted) {
      // 삭제 확인 다이얼로그 표시
      final confirmed = await BookmarkDeleteDialog.show(
        context,
        hearitTitle: hearit.title,
      );

      if (confirmed && mounted) {
        final playerController = context.read<HearitPlayerController>();
        final success = await _viewModel.deleteBookmark(hearit.bookmarkId);

        if (success) {
          // 플레이리스트에서도 제거 (플레이리스트 재생 중일 때)
          if (playerController.isPlayingFromPlaylist) {
            await playerController.removeFromPlaylist(hearit.hearitId);
          }

          // Analytics: 북마크 삭제
          AnalyticsProvider.logger.logEvent(
            AnalyticsEventNames.libraryBookmarkDeleted,
            params: {
              AnalyticsParamKeys.itemId: hearit.hearitId,
              AnalyticsParamKeys.itemName: hearit.title,
            },
          );

          if (mounted) {
            ScaffoldMessenger.of(context).showSnackBar(
              const SnackBar(
                content: Text('북마크가 삭제되었습니다'),
                duration: Duration(seconds: 2),
              ),
            );
          }
        } else if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(
              content: Text('북마크 삭제에 실패했습니다'),
              backgroundColor: AppColors.error,
              duration: Duration(seconds: 2),
            ),
          );
        }
      }
    }
  }

  void _handlePlayAll() async {
    if (_viewModel.bookmarks.isEmpty) return;

    // Analytics: 전체재생 클릭
    AnalyticsProvider.logger.logEvent(
      AnalyticsEventNames.libraryPlayAllClicked,
      params: {AnalyticsParamKeys.bookmarkCount: _viewModel.bookmarks.length},
    );

    final playerController = context.read<HearitPlayerController>();

    try {
      // 북마크 목록을 PlaylistItem으로 변환
      final playlist = _viewModel.bookmarks.map((bookmark) {
        return PlaylistItem(
          hearitId: bookmark.hearitId,
          title: bookmark.title,
          sourceName: bookmark.sources.isNotEmpty
              ? bookmark.sources.first.sourceName
              : 'hEARit',
          playTimeSeconds: bookmark.playTime,
          categoryColorCode: bookmark.category.colorCode,
          audioUrl: null, // 재생 시점에 lazy loading
        );
      }).toList();

      // 플레이리스트 로드 (첫 번째 항목부터 재생)
      await playerController.loadPlaylist(playlist: playlist, startIndex: 0);
    } catch (e) {
      debugPrint('전체재생 실패: $e');
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('재생 중 오류가 발생했습니다'),
            backgroundColor: AppColors.error,
            duration: Duration(seconds: 2),
          ),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(backgroundColor: AppColors.hearitBlack, body: _buildBody());
  }

  Widget _buildBody() {
    // 초기 로딩 상태 (북마크가 없을 때만)
    if (_viewModel.isLoading && _viewModel.bookmarks.isEmpty) {
      return Center(
        child: CircularProgressIndicator(color: AppColors.hearitPurple2),
      );
    }

    // 게스트 상태 (401/403)
    if (_viewModel.isGuest) {
      return Column(
        children: [
          // 헤더 (기본 프로필) - 고정
          LibraryHeader(profile: null, isGuest: true),
          // 북마크 섹션 헤더 - 고정
          BookmarkSectionHeader(
            totalCount: 0,
            onPlayAll: () {}, // No-op for guests
          ),
          // 게스트 메시지 UI - 스크롤 가능
          Expanded(child: _buildGuestState()),
        ],
      );
    }

    // 에러 상태 (북마크가 없을 때만)
    if (_viewModel.error != null && _viewModel.bookmarks.isEmpty) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              Icons.error_outline,
              size: 64,
              color: Colors.white.withValues(alpha: 0.5),
            ),
            const SizedBox(height: 16),
            Text(
              '데이터를 불러올 수 없습니다',
              style: TextStyle(
                color: Colors.white.withValues(alpha: 0.7),
                fontSize: 16,
              ),
            ),
            const SizedBox(height: 24),
            ElevatedButton(
              onPressed: () => _viewModel.loadInitialData(),
              style: ElevatedButton.styleFrom(
                backgroundColor: AppColors.hearitPurple2,
                foregroundColor: Colors.white,
              ),
              child: const Text('다시 시도'),
            ),
          ],
        ),
      );
    }

    // 정상 상태 (헤더 고정 + 리스트 스크롤)
    return Column(
      children: [
        // 헤더 (프로필 + 그라데이션) - 고정
        LibraryHeader(profile: _viewModel.profile, isGuest: false),
        // 북마크 섹션 헤더 - 고정
        BookmarkSectionHeader(
          totalCount: _viewModel.totalElements,
          onPlayAll: _viewModel.bookmarks.isEmpty ? null : _handlePlayAll,
        ),
        // 북마크 리스트 - 스크롤 가능
        Expanded(
          child: RefreshIndicator(
            color: AppColors.hearitPurple2,
            backgroundColor: AppColors.hearitBlack,
            onRefresh: _handleRefresh,
            child: _buildScrollableContent(),
          ),
        ),
      ],
    );
  }

  Widget _buildScrollableContent() {
    return CustomScrollView(
      controller: _scrollController,
      physics: const AlwaysScrollableScrollPhysics(),
      slivers: [
        // 북마크 리스트 또는 빈 상태
        if (_viewModel.isEmpty)
          SliverFillRemaining(child: _buildEmptyState())
        else
          SliverList(
            delegate: SliverChildBuilderDelegate(
              (context, index) {
                if (index < _viewModel.bookmarks.length) {
                  final hearit = _viewModel.bookmarks[index];
                  return BookmarkedHearitCard(
                    hearit: hearit,
                    onTap: () => _handleCardTap(hearit),
                    onMenuTap: () => _handleMenuTap(hearit),
                  );
                } else if (_viewModel.isLoadingMore) {
                  // 로딩 인디케이터
                  return const Padding(
                    padding: EdgeInsets.all(16.0),
                    child: Center(
                      child: CircularProgressIndicator(
                        color: AppColors.hearitPurple2,
                      ),
                    ),
                  );
                }
                return const SizedBox.shrink();
              },
              childCount:
                  _viewModel.bookmarks.length +
                  (_viewModel.isLoadingMore ? 1 : 0),
            ),
          ),
        // 하단 여백
        SliverPadding(
          padding: EdgeInsets.only(
            bottom: 20 + MediaQuery.of(context).padding.bottom,
          ),
        ),
      ],
    );
  }

  Widget _buildEmptyState() {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.start,
        children: [
          const Spacer(flex: 1), // 상단 여백
          Icon(
            Icons.bookmark_border,
            size: 80,
            color: Colors.white.withValues(alpha: 0.3),
          ),
          const SizedBox(height: 16),
          Text(
            '아직 북마크된 팟캐스트가 없습니다',
            style: TextStyle(
              color: AppColors.gray3,
              fontSize: 16,
              fontWeight: FontWeight.w500,
            ),
          ),
          const SizedBox(height: 8),
          Text(
            '상세화면에서 마음에 드는\n팟캐스트를 북마크해보세요!',
            textAlign: TextAlign.center,
            style: TextStyle(
              color: Colors.white.withValues(alpha: 0.5),
              fontSize: 14,
            ),
          ),
          const Spacer(flex: 2), // 하단 여백
        ],
      ),
    );
  }

  Widget _buildGuestState() {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 32),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          const Spacer(flex: 1), // 상단 여백
          RichText(
            textAlign: TextAlign.center,
            text: TextSpan(
              style: const TextStyle(
                color: AppColors.gray4,
                fontSize: 18,
                fontWeight: FontWeight.bold,
                height: 1.5,
              ),
              children: [
                TextSpan(
                  text: '히어릿',
                  style: TextStyle(
                    color: AppColors.hearitPurple1,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const TextSpan(text: '을 모아서 듣고 싶다면,\n로그인을 해주세요!'),
              ],
            ),
          ),
          const SizedBox(height: 32),
          SizedBox(
            width: double.infinity,
            height: 48,
            child: ElevatedButton(
              onPressed: () {
                Navigator.of(context, rootNavigator: true).pushAndRemoveUntil(
                  MaterialPageRoute(builder: (_) => const LoginScreen()),
                  (route) => false,
                );
              },
              style: ElevatedButton.styleFrom(
                backgroundColor: AppColors.hearitPurple3,
                foregroundColor: AppColors.gray4,
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(8),
                ),
                elevation: 0,
              ),
              child: const Text(
                '로그인 하러가기',
                style: TextStyle(fontSize: 18, fontWeight: FontWeight.w600),
              ),
            ),
          ),
          const Spacer(flex: 2), // 하단 여백
        ],
      ),
    );
  }

  /// BookmarkedHearit을 HearitDetail로 변환
  detail.HearitDetail _convertToHearitDetail(BookmarkedHearit hearit) {
    return detail.HearitDetail(
      id: hearit.hearitId,
      title: hearit.title,
      summary: hearit.summary,
      sources: hearit.sources
          .map(
            (s) => detail.HearitSource(
              sourceName: s.sourceName,
              sourceUrl: s.sourceUrl,
            ),
          )
          .toList(),
      playTime: Duration(seconds: hearit.playTime),
      lastPlayTime: hearit.lastPlayTime != null
          ? Duration(milliseconds: hearit.lastPlayTime!)
          : null,
      createdAt: DateTime.now(), // API 응답에 없으므로 현재 시간 사용
      isBookmarked: true, // 북마크 리스트에서 왔으므로 항상 true
      bookmarkId: hearit.bookmarkId,
      category: detail.HearitCategory(
        id: hearit.category.id,
        name: hearit.category.name,
        color: _parseColor(hearit.category.colorCode),
      ),
      keywords: [], // API 응답에 없으므로 빈 리스트
    );
  }

  /// 색상 코드를 Color로 변환
  Color _parseColor(String colorCode) {
    try {
      final hexColor = colorCode.replaceAll('#', '');
      return Color(int.parse('FF$hexColor', radix: 16));
    } catch (e) {
      return AppColors.hearitPurple2;
    }
  }
}
