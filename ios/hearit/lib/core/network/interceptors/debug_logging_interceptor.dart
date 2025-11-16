import 'package:dio/dio.dart';
import 'package:flutter/foundation.dart';

/// Logs API requests/responses only in non-release builds.
class DebugLoggingInterceptor extends LogInterceptor {
  DebugLoggingInterceptor()
    : super(
        request: true,
        requestHeader: true,
        requestBody: true,
        responseHeader: false,
        responseBody: true,
        error: true,
        logPrint: (obj) => debugPrint(obj.toString()),
      );
}
