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

  @override
  void onError(DioException err, ErrorInterceptorHandler handler) async {
    if (err.response?.statusCode != 401 || _isRefreshing) {
      return handler.next(err);
    }

    _isRefreshing = true;
    try {
      final tokens = await _storageService.getTokens();
      if (tokens == null) {
        _eventController.add(AuthEvent.tokenRefreshFailed);
        return handler.next(err);
      }

      // 별도 Dio 인스턴스로 refresh 호출 (인터셉터 루프 방지)
      final refreshDio = Dio();
      final response = await refreshDio.post(
        '${ApiConfig.defaultBaseUrl}/api/v1/auth/token/refresh',
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
      final retryResponse = await Dio().fetch(retryOptions);
      return handler.resolve(retryResponse);
    } catch (e) {
      _eventController.add(AuthEvent.tokenRefreshFailed);
      return handler.next(err);
    } finally {
      _isRefreshing = false;
    }
  }
}
