class ApiException implements Exception {
  ApiException._(
    this.message, {
    this.statusCode,
    this.uri,
    this.innerException,
  });

  factory ApiException.network(Object error, {Uri? uri}) =>
      ApiException._('네트워크 연결에 실패했습니다.', innerException: error, uri: uri);

  factory ApiException.timeout({Uri? uri}) =>
      ApiException._('요청이 시간 초과되었습니다.', uri: uri);

  factory ApiException.server(int statusCode, String body, {Uri? uri}) =>
      ApiException._('서버 요청이 실패했습니다.', statusCode: statusCode, uri: uri);

  factory ApiException.unexpected(Object error, {Uri? uri}) =>
      ApiException._('알 수 없는 오류가 발생했습니다.', innerException: error, uri: uri);

  final String message;
  final int? statusCode;
  final Uri? uri;
  final Object? innerException;

  @override
  String toString() {
    final buffer = StringBuffer('ApiException: $message');
    if (statusCode != null) buffer.write(' (status: $statusCode)');
    if (uri != null) buffer.write(', uri: $uri');
    if (innerException != null) buffer.write(', inner: $innerException');
    return buffer.toString();
  }
}
