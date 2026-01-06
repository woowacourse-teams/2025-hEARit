import 'dart:io';

import 'package:dio/dio.dart';
import 'package:flutter/foundation.dart';

import 'api_config.dart';
import 'api_exception.dart';
import 'interceptors/debug_logging_interceptor.dart';
import 'interceptors/device_uuid_interceptor.dart';

class ApiClient {
  ApiClient({
    ApiConfig config = const ApiConfig(),
    Duration connectTimeout = const Duration(seconds: 5),
    Duration sendTimeout = const Duration(seconds: 10),
    Duration receiveTimeout = const Duration(seconds: 15),
    Dio? dio,
  }) : _config = config,
       _dio =
           dio ??
           Dio(
             BaseOptions(
               baseUrl: config.baseUrl,
               connectTimeout: connectTimeout,
               receiveTimeout: receiveTimeout,
               sendTimeout: sendTimeout,
               headers: _defaultHeaders,
               responseType: ResponseType.json,
             ),
           ) {
    _dio.interceptors.add(DeviceUUIDInterceptor());
    if (!kReleaseMode) {
      _dio.interceptors.add(DebugLoggingInterceptor());
    }
  }

  final ApiConfig _config;
  final Dio _dio;

  static const Map<String, String> _defaultHeaders = {
    HttpHeaders.acceptHeader: 'application/json',
    HttpHeaders.contentTypeHeader: 'application/json',
  };

  Future<T> get<T>(
    String path, {
    Map<String, dynamic>? queryParameters,
    Map<String, String>? headers,
    Options? options,
    T Function(dynamic data)? parser,
  }) async {
    final uri = _config.resolve(path, queryParameters: queryParameters);
    final response = await _send(
      () => _dio.getUri<dynamic>(uri, options: _mergeOptions(options, headers)),
      uri,
    );
    return _parse(response.data, parser);
  }

  Future<T> post<T>(
    String path, {
    Map<String, dynamic>? queryParameters,
    Object? body,
    Map<String, String>? headers,
    Options? options,
    T Function(dynamic data)? parser,
  }) async {
    final uri = _config.resolve(path, queryParameters: queryParameters);
    final response = await _send(
      () => _dio.postUri<dynamic>(
        uri,
        data: body,
        options: _mergeOptions(options, headers),
      ),
      uri,
    );
    return _parse(response.data, parser);
  }

  Future<T> delete<T>(
    String path, {
    Map<String, dynamic>? queryParameters,
    Object? body,
    Map<String, String>? headers,
    Options? options,
    T Function(dynamic data)? parser,
  }) async {
    final uri = _config.resolve(path, queryParameters: queryParameters);
    final response = await _send(
      () => _dio.deleteUri<dynamic>(
        uri,
        data: body,
        options: _mergeOptions(options, headers),
      ),
      uri,
    );
    return _parse(response.data, parser);
  }

  Future<T> upload<T>(
    String path, {
    required FormData formData,
    Map<String, dynamic>? queryParameters,
    Map<String, String>? headers,
    Options? options,
    T Function(dynamic data)? parser,
  }) async {
    final uri = _config.resolve(path, queryParameters: queryParameters);
    final uploadOptions = Options(
      method: options?.method ?? 'POST',
      headers: options?.headers,
      responseType: options?.responseType,
      contentType: Headers.multipartFormDataContentType,
      extra: options?.extra,
      followRedirects: options?.followRedirects,
      listFormat: options?.listFormat,
      maxRedirects: options?.maxRedirects,
      receiveDataWhenStatusError: options?.receiveDataWhenStatusError,
      receiveTimeout: options?.receiveTimeout,
      requestEncoder: options?.requestEncoder,
      responseDecoder: options?.responseDecoder,
      sendTimeout: options?.sendTimeout,
      validateStatus: options?.validateStatus,
    );
    final response = await _send(
      () => _dio.postUri<dynamic>(
        uri,
        data: formData,
        options: _mergeOptions(
          uploadOptions,
          headers,
          leadingHeaders: {
            HttpHeaders.contentTypeHeader: Headers.multipartFormDataContentType,
          },
        ),
      ),
      uri,
    );
    return _parse(response.data, parser);
  }

  Future<Response<dynamic>> _send(
    Future<Response<dynamic>> Function() action,
    Uri uri,
  ) async {
    try {
      final response = await action();
      _throwIfError(response, uri);
      return response;
    } on DioException catch (error) {
      throw _mapDioException(error, uri);
    } catch (error) {
      throw ApiException.unexpected(error, uri: uri);
    }
  }

  void _throwIfError(Response response, Uri uri) {
    final status = response.statusCode ?? 0;
    if (status < 400) return;
    _logFailure(response, uri);
    throw ApiException.server(status, response.data.toString(), uri: uri);
  }

  ApiException _mapDioException(DioException error, Uri uri) {
    if (error.error is SocketException) {
      return ApiException.network(error, uri: uri);
    }
    if (error.type == DioExceptionType.connectionError) {
      final underlying = error.error;
      final isTimeoutLike =
          underlying is SocketException &&
          underlying.osError?.message.contains('timed out') == true;
      if (isTimeoutLike) return ApiException.timeout(uri: uri);
      return ApiException.network(error, uri: uri);
    }
    if (error.type == DioExceptionType.connectionTimeout ||
        error.type == DioExceptionType.receiveTimeout ||
        error.type == DioExceptionType.sendTimeout) {
      return ApiException.timeout(uri: uri);
    }
    if (error.type == DioExceptionType.badResponse && error.response != null) {
      final response = error.response!;
      _logFailure(response, uri);
      return ApiException.server(
        response.statusCode ?? 500,
        response.data.toString(),
        uri: uri,
      );
    }
    return ApiException.unexpected(error, uri: uri);
  }

  T _parse<T>(Object? decoded, T Function(dynamic data)? parser) {
    if (parser != null) return parser(decoded);
    // Handle void/Null types for DELETE and other methods with no return value
    if (T.toString() == 'void' || T == Null) return null as T;
    if (T == Map<String, dynamic>) return decoded as T;
    if (T == List<dynamic>) return decoded as T;
    if (T == String) return decoded.toString() as T;
    if (decoded is T) return decoded;
    throw ApiException.unexpected('Parser missing for $T');
  }

  Options _mergeOptions(
    Options? options,
    Map<String, String>? headers, {
    Map<String, dynamic>? leadingHeaders,
  }) {
    final mergedHeaders = <String, dynamic>{
      ...?leadingHeaders,
      ..._defaultHeaders,
      ...?headers,
      ...?options?.headers,
    };
    return Options(
      method: options?.method,
      headers: mergedHeaders,
      responseType: options?.responseType,
      contentType: options?.contentType,
      extra: options?.extra,
      followRedirects: options?.followRedirects,
      listFormat: options?.listFormat,
      maxRedirects: options?.maxRedirects,
      receiveDataWhenStatusError: options?.receiveDataWhenStatusError,
      receiveTimeout: options?.receiveTimeout ?? _dio.options.receiveTimeout,
      requestEncoder: options?.requestEncoder,
      responseDecoder: options?.responseDecoder,
      sendTimeout: options?.sendTimeout ?? _dio.options.sendTimeout,
      validateStatus: options?.validateStatus,
    );
  }

  void _logFailure(Response response, Uri uri) {
    if (!kDebugMode) return;
    debugPrint(
      'API ${response.requestOptions.method} ${uri.toString()} '
      'failed: ${response.statusCode} ${response.statusMessage}',
    );
  }

  Future<void> dispose() async {
    _dio.interceptors.clear();
    _dio.close(force: true);
  }
}
