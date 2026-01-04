/// 북마크된 히어릿 데이터 모델
class BookmarkedHearit {
  final int hearitId;
  final int bookmarkId;
  final String title;
  final String summary;
  final int playTime; // 전체 재생 시간 (초 단위)
  final int lastPlayTime; // 마지막 재생 위치 (밀리초 단위)
  final bool isFinished;
  final List<HearitSource> sources;
  final BookmarkCategory category;

  BookmarkedHearit({
    required this.hearitId,
    required this.bookmarkId,
    required this.title,
    required this.summary,
    required this.playTime,
    required this.lastPlayTime,
    required this.isFinished,
    required this.sources,
    required this.category,
  });

  factory BookmarkedHearit.fromJson(Map<String, dynamic> json) {
    return BookmarkedHearit(
      hearitId: json['hearitId'] as int,
      bookmarkId: json['bookmarkId'] as int,
      title: json['title'] as String,
      summary: json['summary'] as String,
      playTime: json['playTime'] as int,
      lastPlayTime: json['lastPlayTime'] as int,
      isFinished: json['isFinished'] as bool,
      sources: (json['sources'] as List<dynamic>)
          .map(
            (source) => HearitSource.fromJson(source as Map<String, dynamic>),
          )
          .toList(),
      category: BookmarkCategory.fromJson(
        json['category'] as Map<String, dynamic>,
      ),
    );
  }

  /// 재생 진행률 계산 (0.0 ~ 1.0)
  double get progress {
    if (playTime <= 0) return 0.0;
    final progressValue = (lastPlayTime / 1000) / playTime;
    return progressValue.clamp(0.0, 1.0);
  }

  /// 재생 시간 포맷 (MM:SS 또는 HH:MM:SS)
  String get formattedPlayTime {
    return _formatDuration(playTime);
  }

  /// 마지막 재생 위치 포맷
  String get formattedLastPlayTime {
    return _formatDuration((lastPlayTime / 1000).round());
  }

  String _formatDuration(int seconds) {
    final hours = seconds ~/ 3600;
    final minutes = (seconds % 3600) ~/ 60;
    final secs = seconds % 60;

    if (hours > 0) {
      return '${hours.toString().padLeft(2, '0')}:${minutes.toString().padLeft(2, '0')}:${secs.toString().padLeft(2, '0')}';
    } else {
      return '${minutes.toString().padLeft(2, '0')}:${secs.toString().padLeft(2, '0')}';
    }
  }
}

/// 히어릿 출처 정보
class HearitSource {
  final String sourceName;
  final String sourceUrl;

  HearitSource({required this.sourceName, required this.sourceUrl});

  factory HearitSource.fromJson(Map<String, dynamic> json) {
    return HearitSource(
      sourceName: json['sourceName'] as String,
      sourceUrl: json['sourceUrl'] as String,
    );
  }
}

/// 북마크 카테고리 정보
class BookmarkCategory {
  final int id;
  final String name;
  final String colorCode;

  BookmarkCategory({
    required this.id,
    required this.name,
    required this.colorCode,
  });

  factory BookmarkCategory.fromJson(Map<String, dynamic> json) {
    return BookmarkCategory(
      id: json['id'] as int,
      name: json['name'] as String,
      colorCode: json['colorCode'] as String,
    );
  }

  /// 카테고리 이름의 첫 글자 이니셜
  String get initial {
    if (name.isEmpty) return '?';
    return name[0].toUpperCase();
  }
}

/// 북마크 리스트 응답 모델 (페이징 포함)
class BookmarkListResponse {
  final List<BookmarkedHearit> content;
  final int page;
  final int size;
  final int totalPages;
  final int totalElements;
  final bool isFirst;
  final bool isLast;

  BookmarkListResponse({
    required this.content,
    required this.page,
    required this.size,
    required this.totalPages,
    required this.totalElements,
    required this.isFirst,
    required this.isLast,
  });

  factory BookmarkListResponse.fromJson(Map<String, dynamic> json) {
    return BookmarkListResponse(
      content: (json['content'] as List<dynamic>)
          .map(
            (item) => BookmarkedHearit.fromJson(item as Map<String, dynamic>),
          )
          .toList(),
      page: json['page'] as int,
      size: json['size'] as int,
      totalPages: json['totalPages'] as int,
      totalElements: json['totalElements'] as int,
      isFirst: json['isFirst'] as bool,
      isLast: json['isLast'] as bool,
    );
  }

  /// 다음 페이지가 있는지 확인
  bool get hasMore => !isLast;
}
