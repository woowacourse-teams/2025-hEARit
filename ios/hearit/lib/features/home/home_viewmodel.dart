import 'package:flutter/material.dart';
import '../auth/auth_viewmodel.dart';
import '../detail/hearit_detail.dart';
import 'home_models.dart';
import 'home_repository.dart';

class HomeViewModel extends ChangeNotifier {
  HomeViewModel({
    HomeRepository? repository,
    required AuthViewModel authViewModel,
    VoidCallback? onUnauthorized,
  }) : _repository = repository ?? HomeRepository(),
       _authViewModel = authViewModel,
       _onUnauthorized = onUnauthorized;

  final HomeRepository _repository;
  // ignore: unused_field
  final AuthViewModel _authViewModel;
  final VoidCallback? _onUnauthorized;

  List<RecommendCardData> _todayRecommendedHearits = const [];
  List<ListeningCardData> _listeningNowHearits = const [];
  List<ListeningCardData> _recentlyAddedHearits = const [];
  List<ListeningCardData> _bookmarkedHearits = const [];
  List<CategorySectionData> _curatedCategoryHearits = const [];

  String? _error;
  bool _loading = false;
  bool _shouldShowBookmarks = false;

  List<RecommendCardData> get todayRecommendedHearits =>
      _todayRecommendedHearits;
  List<ListeningCardData> get listeningNowHearits => _listeningNowHearits;
  List<ListeningCardData> get recentlyAddedHearits => _recentlyAddedHearits;
  List<ListeningCardData> get bookmarkedHearits => _bookmarkedHearits;
  List<CategorySectionData> get curatedCategoryHearits =>
      _curatedCategoryHearits;
  bool get isLoading => _loading;
  String? get error => _error;
  bool get shouldShowBookmarks => _shouldShowBookmarks;

  Future<void> loadHome() async {
    _loading = true;
    _error = null;
    notifyListeners();

    final List<String> failures = [];

    // 1. 토큰 확인 및 인증 체크
    final hasToken = await _repository.hasValidToken();
    bool shouldFetchBookmarks = false;

    if (hasToken) {
      // 2. 토큰이 있으면 /api/v1/auth/check 호출
      final authCheckResult = await _repository.checkAuth();

      if (authCheckResult == AuthCheckStatus.unauthorized) {
        // 401 → 로그인 화면으로 이동
        _onUnauthorized?.call();
        _loading = false;
        notifyListeners();
        return; // 더 이상 진행하지 않음
      } else if (authCheckResult == AuthCheckStatus.ok) {
        // 200 → 북마크 섹션 표시 및 API 호출 허용
        _shouldShowBookmarks = true;
        shouldFetchBookmarks = true;
      }
      // error일 경우 (네트워크 에러 등) → 북마크 섹션 숨김, API 호출 안 함
    } else {
      // 토큰 없음 → 북마크 섹션 숨김
      _shouldShowBookmarks = false;
    }

    // 3. API 호출 함수들
    Future<List<RecommendCardData>> fetchRecommendations() async {
      try {
        return await _repository.fetchRecommendations();
      } catch (error, stack) {
        debugPrint('HomeViewModel.fetchRecommendations error: $error\n$stack');
        failures.add('추천');
        return const [];
      }
    }

    Future<List<ListeningCardData>> fetchListeningNow() async {
      try {
        return await _repository.fetchListeningNow();
      } catch (error, stack) {
        debugPrint('HomeViewModel.fetchListeningNow error: $error\n$stack');
        failures.add('듣는 중');
        return const [];
      }
    }

    Future<List<ListeningCardData>> fetchRecentlyAdded() async {
      try {
        return await _repository.fetchRecentlyAdded();
      } catch (error, stack) {
        debugPrint('HomeViewModel.fetchRecentlyAdded error: $error\n$stack');
        failures.add('최근 추가');
        return const [];
      }
    }

    Future<List<ListeningCardData>> fetchBookmarked() async {
      if (!shouldFetchBookmarks) {
        return const []; // 인증 실패 또는 토큰 없음 → 빈 리스트
      }
      try {
        return await _repository.fetchBookmarked();
      } catch (error, stack) {
        debugPrint('HomeViewModel.fetchBookmarked error: $error\n$stack');
        failures.add('북마크');
        return const [];
      }
    }

    Future<List<CategorySectionData>> fetchCategoryRecommendations() async {
      try {
        return await _repository.fetchCategoryRecommendations();
      } catch (error, stack) {
        debugPrint(
          'HomeViewModel.fetchCategoryRecommendations error: $error\n$stack',
        );
        failures.add('카테고리 추천');
        return const [];
      }
    }

    // 4. 병렬 API 호출
    try {
      final results = await Future.wait([
        fetchRecommendations(),
        fetchListeningNow(),
        fetchRecentlyAdded(),
        fetchBookmarked(),
        fetchCategoryRecommendations(),
      ]);

      _todayRecommendedHearits = results[0] as List<RecommendCardData>;
      _listeningNowHearits = results[1] as List<ListeningCardData>;
      _recentlyAddedHearits = results[2] as List<ListeningCardData>;
      _bookmarkedHearits = results[3] as List<ListeningCardData>;
      _curatedCategoryHearits = results[4] as List<CategorySectionData>;

      if (failures.isNotEmpty) {
        _error = '${failures.join(', ')} 데이터를 불러오지 못했습니다.';
      }
    } finally {
      _loading = false;
      notifyListeners();
    }
  }

  HearitDetail toHearitDetailFromRecommend(RecommendCardData data) =>
      HearitDetail.fromSummaryStub(
        id: data.id,
        title: data.title,
        categoryName: data.categoryName,
        accentColor: data.categoryColor,
        createdAt: data.createdAt,
      );

  HearitDetail toHearitDetailFromListening(ListeningCardData data) =>
      HearitDetail.fromSummaryStub(
        id: data.id,
        title: data.description,
        categoryName: data.title,
        accentColor: data.backgroundColor,
        createdAt: data.createdAt,
      );

  HearitDetail toHearitDetailFromCategory(
    CategorySectionData section,
    CategoryPodcastData podcast,
  ) => HearitDetail.fromSummaryStub(
    id: podcast.id,
    title: podcast.title,
    categoryName: section.categoryName,
    accentColor: section.accentColor,
    createdAt: podcast.createdAt,
  );
}
