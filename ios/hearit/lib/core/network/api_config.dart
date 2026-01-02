class ApiConfig {
  const ApiConfig({this.baseUrl = defaultBaseUrl});

  static const String defaultBaseUrl = 'https://hearit-dev.o-r.kr';

  final String baseUrl;

  /// Returns a normalized URI using the configured base URL.
  Uri resolve(String path, {Map<String, dynamic>? queryParameters}) {
    final base = Uri.parse(baseUrl);
    final cleanedPath = _joinPaths(base.path, path);
    return base.replace(
      path: cleanedPath,
      queryParameters: _normalizeQuery(queryParameters),
    );
  }

  Map<String, String>? _normalizeQuery(Map<String, dynamic>? query) {
    if (query == null || query.isEmpty) return null;
    return query.map((key, value) => MapEntry(key, value.toString()));
  }

  String _joinPaths(String basePath, String path) {
    final trimmedBase = basePath.endsWith('/')
        ? basePath.substring(0, basePath.length - 1)
        : basePath;
    final trimmedPath = path.startsWith('/') ? path.substring(1) : path;
    if (trimmedBase.isEmpty) return '/$trimmedPath';
    return '$trimmedBase/$trimmedPath';
  }
}
