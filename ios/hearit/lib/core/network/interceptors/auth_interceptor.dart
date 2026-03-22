import 'dart:async';

import 'package:dio/dio.dart';

import '../../../features/auth/models/auth_event.dart';
import '../../../features/auth/models/auth_tokens.dart';
import '../../../features/auth/services/auth_storage_service.dart';
import '../api_config.dart';

class AuthInterceptor extends Interceptor {
  AuthInterceptor({
    required AuthStorageService storageService,
    required StreamController<AuthEvent> eventController,
  })  : _storageService = storageService,
        _eventController = eventController;

  final AuthStorageService _storageService;
  final StreamController<AuthEvent> _eventController;
  bool _isRefreshing = false;

  void _emitRefreshFailed() {
    if (!_eventController.isClosed) {
      _eventController.add(AuthEvent.tokenRefreshFailed);
    }
  }

  @override
  void onError(DioException err, ErrorInterceptorHandler handler) async {
    // refresh 엔드포인트 자체가 401을 반환할 경우 재진입 방지
    const refreshPath = '/api/v1/auth/token/refresh';
    final requestPath = err.requestOptions.path;
    final isRefreshRequest =
        requestPath == refreshPath || requestPath.endsWith(refreshPath);

    if (err.response?.statusCode != 401 || _isRefreshing || isRefreshRequest) {
      return handler.next(err);
    }

    _isRefreshing = true;
    try {
      final tokens = await _storageService.getTokens();
      if (tokens == null) {
        _emitRefreshFailed();
        return handler.next(err);
      }

      // refresh 요청: 별도 Dio 인스턴스로 인터셉터 루프 방지
      // baseUrl은 원래 요청의 baseUrl을 따라 환경(스테이징/프로덕션)을 존중
      final baseUrl = err.requestOptions.baseUrl.isNotEmpty
          ? err.requestOptions.baseUrl
          : ApiConfig.defaultBaseUrl;
      final refreshDio = Dio(
        BaseOptions(
          connectTimeout: err.requestOptions.connectTimeout,
          receiveTimeout: err.requestOptions.receiveTimeout,
          sendTimeout: err.requestOptions.sendTimeout,
        ),
      );
      final response = await refreshDio.post(
        '${baseUrl}api/v1/auth/token/refresh',
        data: {'refreshToken': tokens.refreshToken},
      );
      final newAccessToken = response.data['accessToken'] as String;

      // 새 accessToken 저장 (refreshToken은 그대로 유지)
      await _storageService.saveTokens(
        AuthTokens(
          accessToken: newAccessToken,
          refreshToken: tokens.refreshToken,
        ),
      );

      // 원래 요청을 새 accessToken으로 재시도
      final retryOptions = err.requestOptions;
      retryOptions.headers['Authorization'] = 'Bearer $newAccessToken';
      final retryDio = Dio(
        BaseOptions(
          baseUrl: retryOptions.baseUrl,
          connectTimeout: retryOptions.connectTimeout,
          receiveTimeout: retryOptions.receiveTimeout,
          sendTimeout: retryOptions.sendTimeout,
          responseType: retryOptions.responseType,
          contentType: retryOptions.contentType,
          followRedirects: retryOptions.followRedirects,
          validateStatus: retryOptions.validateStatus,
          receiveDataWhenStatusError: retryOptions.receiveDataWhenStatusError,
          headers: retryOptions.headers,
        ),
      );
      final retryResponse = await retryDio.fetch(retryOptions);
      return handler.resolve(retryResponse);
    } catch (e) {
      _emitRefreshFailed();
      return handler.next(err);
    } finally {
      _isRefreshing = false;
    }
  }
}
