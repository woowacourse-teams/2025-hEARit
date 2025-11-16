import '../../core/network/api_client.dart';
import 'search_models.dart';

class SearchRepository {
  SearchRepository({ApiClient? apiClient})
    : _apiClient = apiClient ?? ApiClient();

  final ApiClient _apiClient;

  Future<List<SearchHearit>> searchHearits(
    String term, {
    int page = 0,
    int size = 20,
  }) async {
    final Map<String, dynamic> data = await _apiClient.get<Map<String, dynamic>>(
      '/api/v1/hearits/search',
      queryParameters: {
        'searchTerm': term,
        'page': page,
        'size': size,
      },
      parser: (raw) => raw as Map<String, dynamic>? ?? <String, dynamic>{},
    );
    final List<dynamic> content = data['content'] as List<dynamic>? ?? const [];
    return content
        .whereType<Map<String, dynamic>>()
        .map(_mapHearit)
        .toList();
  }

  SearchHearit _mapHearit(Map<String, dynamic> json) {
    final keywords = (json['keywords'] as List<dynamic>? ?? const [])
        .whereType<Map<String, dynamic>>()
        .map((e) => e['name'] as String? ?? '')
        .where((name) => name.isNotEmpty)
        .toList();

    return SearchHearit(
      id: _asInt(json['id']),
      title: json['title'] as String? ?? '',
      playTimeSeconds: (json['playTime'] as num?)?.toInt() ?? 0,
      lastPlayTimeSeconds:
          (json['lastPlayTime'] as num?) != null
              ? (json['lastPlayTime'] as num).toInt()
              : null,
      isFinished: json['isFinished'] as bool?,
      keywords: keywords,
    );
  }

  int _asInt(dynamic value) {
    if (value is int) return value;
    return int.tryParse(value.toString()) ?? 0;
  }
}
