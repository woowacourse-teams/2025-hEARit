/// 플레이리스트 아이템 모델
///
/// 북마크 API에서 받은 기본 정보를 저장하고,
/// 오디오 URL은 재생 시점에 lazy loading합니다.
class PlaylistItem {
  /// 히어릿 ID (오디오 URL 조회용)
  final int hearitId;

  /// 히어릿 제목
  final String title;

  /// 소스명 (sources 배열의 첫 번째 항목)
  final String sourceName;

  /// 재생 시간 (초 단위)
  final int playTimeSeconds;

  /// 카테고리 색상 코드 (예: "#FF5733")
  final String categoryColorCode;

  /// 오디오 원본 URL (재생 시점에 API로 가져옴)
  String? audioUrl;

  PlaylistItem({
    required this.hearitId,
    required this.title,
    required this.sourceName,
    required this.playTimeSeconds,
    required this.categoryColorCode,
    this.audioUrl,
  });

  /// 재생 시간을 MM:SS 형식으로 포맷팅
  String get formattedDuration {
    final minutes = playTimeSeconds ~/ 60;
    final seconds = playTimeSeconds % 60;
    return '$minutes:${seconds.toString().padLeft(2, '0')}';
  }

  /// 오디오 URL이 로드되었는지 확인
  bool get hasAudioUrl => audioUrl != null && audioUrl!.isNotEmpty;

  /// JSON으로 직렬화 (audioUrl은 캐싱 데이터이므로 제외)
  Map<String, dynamic> toJson() {
    return {
      'hearitId': hearitId,
      'title': title,
      'sourceName': sourceName,
      'playTimeSeconds': playTimeSeconds,
      'categoryColorCode': categoryColorCode,
    };
  }

  /// JSON에서 역직렬화
  factory PlaylistItem.fromJson(Map<String, dynamic> json) {
    return PlaylistItem(
      hearitId: json['hearitId'] as int,
      title: json['title'] as String,
      sourceName: json['sourceName'] as String,
      playTimeSeconds: json['playTimeSeconds'] as int,
      categoryColorCode: json['categoryColorCode'] as String,
      audioUrl: null, // 항상 null로 시작 (재생 시 다시 fetch)
    );
  }

  @override
  String toString() {
    return 'PlaylistItem(hearitId: $hearitId, title: $title, sourceName: $sourceName, '
        'playTime: $playTimeSeconds, hasAudioUrl: $hasAudioUrl)';
  }
}
