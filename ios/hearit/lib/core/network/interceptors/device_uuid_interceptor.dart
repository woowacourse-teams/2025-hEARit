import 'package:dio/dio.dart';

import '../../device/device_uuid_service.dart';

class DeviceUUIDInterceptor extends Interceptor {
  @override
  void onRequest(
    RequestOptions options,
    RequestInterceptorHandler handler,
  ) async {
    final uuid = await DeviceUUIDService.getUUID();
    options.headers['X-Device-UUID'] = uuid;
    handler.next(options);
  }
}
