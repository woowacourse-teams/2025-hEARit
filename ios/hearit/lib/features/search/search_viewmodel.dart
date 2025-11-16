import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';

import 'search_models.dart';
import 'search_repository.dart';

class SearchViewModel extends ChangeNotifier {
  SearchViewModel({SearchRepository? repository})
    : _repository = repository ?? SearchRepository() {
    searchController.addListener(_handleInputChanged);
  }

  final SearchRepository _repository;
  final TextEditingController searchController = TextEditingController();

  String _query = '';
  String _lastSearchedQuery = '';
  bool _loading = false;
  bool _hasSearched = false;
  String? _error;
  List<SearchHearit> _results = const [];
  final List<_SearchSnapshot> _history = [];

  String get query => _query;
  bool get hasQuery => _query.isNotEmpty;
  bool get isLoading => _loading;
  bool get hasSearched => _hasSearched;
  String? get error => _error;
  List<SearchHearit> get results => _results;
  bool get canGoBack => _history.isNotEmpty;

  void _handleInputChanged() {
    final String newQuery = searchController.text;
    if (newQuery == _query) return;
    _query = newQuery;
    notifyListeners();
  }

  Future<void> submitQuery() async {
    final term = query.trim();
    if (term.isEmpty) {
      _results = const [];
      _error = null;
      _hasSearched = false;
      _lastSearchedQuery = '';
      _history.clear();
      notifyListeners();
      return;
    }

    _pushSnapshot();

    _loading = true;
    _error = null;
    notifyListeners();

    try {
      _results = await _repository.searchHearits(term);
      _hasSearched = true;
      _lastSearchedQuery = term;
    } catch (error, stack) {
      debugPrint('SearchViewModel.submitQuery error: $error\n$stack');
      _error = '검색에 실패했어요. 잠시 후 다시 시도해 주세요.';
    } finally {
      _loading = false;
      notifyListeners();
    }
  }

  void clearQuery() {
    searchController.clear();
    _lastSearchedQuery = '';
    _hasSearched = false;
    _results = const [];
    _error = null;
    _history.clear();
    _handleInputChanged();
  }

  void stepBack() {
    if (_history.isEmpty) return;
    final previous = _history.removeLast();
    _query = previous.searchQuery;
    _lastSearchedQuery = previous.searchQuery;
    searchController.value = TextEditingValue(
      text: previous.searchQuery,
      selection: TextSelection.collapsed(offset: previous.searchQuery.length),
    );
    _results = previous.results;
    _hasSearched = previous.hasSearched;
    _error = previous.error;
    _loading = false;
    notifyListeners();
  }

  void _pushSnapshot() {
    _history.add(
      _SearchSnapshot(
        searchQuery: _lastSearchedQuery,
        results: _results,
        hasSearched: _hasSearched,
        error: _error,
      ),
    );
  }

  @override
  void dispose() {
    searchController.removeListener(_handleInputChanged);
    searchController.dispose();
    super.dispose();
  }
}

class _SearchSnapshot {
  const _SearchSnapshot({
    required this.searchQuery,
    required this.results,
    required this.hasSearched,
    required this.error,
  });

  final String searchQuery;
  final List<SearchHearit> results;
  final bool hasSearched;
  final String? error;
}
