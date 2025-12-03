import 'package:flutter/material.dart';
import '../detail/hearit_detail.dart';
import 'home_models.dart';
import 'home_repository.dart';

class HomeViewModel extends ChangeNotifier {
  HomeViewModel({HomeRepository? repository})
    : _repository = repository ?? HomeRepository();

  final HomeRepository _repository;

  List<RecommendCardData> _todayRecommendedHearits = const [];
  List<ListeningCardData> _listeningNowHearits = const [];
  List<ListeningCardData> _recentlyAddedHearits = const [];
  List<ListeningCardData> _bookmarkedHearits = const [];
  List<CategorySectionData> _curatedCategoryHearits = const [];

  String? _error;
  bool _loading = false;

  List<RecommendCardData> get todayRecommendedHearits =>
      _todayRecommendedHearits;
  List<ListeningCardData> get listeningNowHearits => _listeningNowHearits;
  List<ListeningCardData> get recentlyAddedHearits => _recentlyAddedHearits;
  List<ListeningCardData> get bookmarkedHearits => _bookmarkedHearits;
  List<CategorySectionData> get curatedCategoryHearits =>
      _curatedCategoryHearits;
  bool get isLoading => _loading;
  String? get error => _error;

  Future<void> loadHome() async {
    _loading = true;
    _error = null;
    notifyListeners();

    final List<String> failures = [];

    // Fetch in parallel but isolate failures per section so partial data can render.
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
