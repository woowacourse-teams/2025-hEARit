import 'package:flutter/foundation.dart';

import '../../core/network/api_exception.dart';
import '../setting/models/member_profile.dart';
import '../setting/setting_repository.dart';
import 'library_models.dart';
import 'library_repository.dart';

/// 라이브러리 화면의 상태 관리 및 비즈니스 로직을 담당하는 ViewModel
class LibraryViewModel extends ChangeNotifier {
  LibraryViewModel({
    LibraryRepository? libraryRepository,
    SettingRepository? settingRepository,
  }) : _libraryRepository = libraryRepository ?? LibraryRepository(),
       _settingRepository = settingRepository ?? SettingRepository();

  final LibraryRepository _libraryRepository;
  final SettingRepository _settingRepository;

  // 상태 변수들
  List<BookmarkedHearit> _bookmarks = [];
  MemberProfile? _profile;
  bool _isLoading = false;
  bool _isLoadingMore = false;
  String? _error;
  int _currentPage = 0;
  int _totalElements = 0;
  bool _hasMore = true;
  bool _isGuest = false;
  String? _loadMoreAuthError; // 무한 스크롤 중 인증 에러

  static const int _pageSize = 20;

  // Getters
  List<BookmarkedHearit> get bookmarks => _bookmarks;
  MemberProfile? get profile => _profile;
  bool get isLoading => _isLoading;
  bool get isLoadingMore => _isLoadingMore;
  String? get error => _error;
  int get totalElements => _totalElements;
  bool get hasMore => _hasMore;
  bool get isEmpty => _bookmarks.isEmpty && !_isLoading;
  bool get isGuest => _isGuest;
  String? get loadMoreAuthError => _loadMoreAuthError;

  /// 초기 데이터 로드 (프로필 + 첫 페이지 북마크)
  Future<void> loadInitialData() async {
    if (_isLoading) return;

    _isLoading = true;
    _error = null;
    _bookmarks = []; // 에러 시에도 명확한 빈 상태 표시
    _totalElements = 0;
    _currentPage = 0;
    _hasMore = true;
    _isGuest = false; // Reset guest state on reload
    notifyListeners();

    try {
      // 프로필과 북마크를 병렬로 로드
      final results = await Future.wait([
        _settingRepository.fetchMyProfile(),
        _libraryRepository.fetchBookmarks(page: 0, size: _pageSize),
      ]);

      _profile = results[0] as MemberProfile;
      final response = results[1] as BookmarkListResponse;

      _bookmarks = response.content;
      _totalElements = response.totalElements;
      _hasMore = response.hasMore;
      _currentPage = 0;
      _error = null;
    } on ApiException catch (e) {
      // Check for unauthorized (401) or forbidden (403)
      if (e.statusCode == 401 || e.statusCode == 403) {
        _isGuest = true;
        _error = null; // Don't show error UI for guests
        _profile = null; // Will display default profile
        debugPrint('초기 데이터 로드 실패 (401/403): guest 모드로 전환');
      } else {
        _error = e.toString();
        debugPrint('초기 데이터 로드 실패: $e');
      }
    } catch (e) {
      // Handle non-API errors (generic fallback)
      _error = e.toString();
      debugPrint('초기 데이터 로드 실패: $e');
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  /// 다음 페이지 북마크 로드 (무한 스크롤)
  Future<void> loadMoreBookmarks() async {
    if (_isLoadingMore || !_hasMore || _isLoading) return;

    _isLoadingMore = true;
    notifyListeners();

    try {
      final nextPage = _currentPage + 1;
      final response = await _libraryRepository.fetchBookmarks(
        page: nextPage,
        size: _pageSize,
      );

      _bookmarks.addAll(response.content);
      _currentPage = nextPage;
      _hasMore = response.hasMore;
      _totalElements = response.totalElements;
    } on ApiException catch (e) {
      // 401/403: 세션 만료 또는 인증 실패 → 기존 데이터 유지하고 로딩만 중단
      if (e.statusCode == 401 || e.statusCode == 403) {
        _hasMore = false;
        _loadMoreAuthError = '로그인이 만료됐습니다. 다시 로그인해주세요';
        debugPrint('추가 북마크 로드 중 인증 실패 (401/403): 로딩 중단');
      } else {
        // 다른 API 에러: 기존 데이터 유지
        debugPrint('추가 북마크 로드 실패: $e');
      }
    } catch (e) {
      // 다른 에러: 기존 데이터 유지
      debugPrint('추가 북마크 로드 실패: $e');
    } finally {
      _isLoadingMore = false;
      notifyListeners();
    }
  }

  /// 북마크 삭제
  Future<bool> deleteBookmark(int bookmarkId) async {
    try {
      await _libraryRepository.deleteBookmark(bookmarkId);

      // 로컬 리스트에서도 제거
      _bookmarks.removeWhere((bookmark) => bookmark.bookmarkId == bookmarkId);
      _totalElements = _totalElements > 0 ? _totalElements - 1 : 0;

      notifyListeners();
      return true;
    } catch (e) {
      debugPrint('북마크 삭제 실패: $e');
      return false;
    }
  }

  /// 새로고침 (Pull-to-refresh)
  Future<void> refreshBookmarks() async {
    _currentPage = 0;
    _hasMore = true;
    await loadInitialData();
  }

  /// 프로필만 다시 로드
  Future<void> reloadProfile() async {
    try {
      _profile = await _settingRepository.fetchMyProfile();
      notifyListeners();
    } catch (e) {
      debugPrint('프로필 재로드 실패: $e');
    }
  }

  /// 무한 스크롤 인증 에러 소비
  void clearLoadMoreAuthError() {
    _loadMoreAuthError = null;
  }
}
