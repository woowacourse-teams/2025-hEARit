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

    try {
      final results = await Future.wait([
        _repository.fetchRecommendations(),
        _repository.fetchListeningNow(),
        _repository.fetchRecentlyAdded(),
        _repository.fetchBookmarked(),
        _repository.fetchCategoryRecommendations(),
      ]);

      _todayRecommendedHearits = results[0] as List<RecommendCardData>;
      _listeningNowHearits = results[1] as List<ListeningCardData>;
      _recentlyAddedHearits = results[2] as List<ListeningCardData>;
      _bookmarkedHearits = results[3] as List<ListeningCardData>;
      _curatedCategoryHearits = results[4] as List<CategorySectionData>;
    } catch (error, stack) {
      debugPrint('HomeViewModel.loadHome error: $error\n$stack');
      _error = error.toString();
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
