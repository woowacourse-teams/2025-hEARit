import 'package:flutter/services.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:uuid/uuid.dart';

class DeviceUUIDService {
  DeviceUUIDService._();

  static const _key = 'device_uuid';
  static final FlutterSecureStorage _storage = FlutterSecureStorage();
  static final Uuid _uuid = const Uuid();
  static String? _cachedUuid;

  /// 앱 전체에서 사용할 기기 UUID를 반환합니다.
  static Future<String> getUUID() async {
    if (_cachedUuid != null) return _cachedUuid!;

    try {
      final stored = await _storage.read(key: _key);
      if (stored != null) {
        _cachedUuid = stored;
        return stored;
      }

      final newUuid = _uuid.v4();
      try {
        await _storage.write(key: _key, value: newUuid);
      } on PlatformException catch (error) {
        // Key already exists or other keychain quirks: try reading back
        if (error.code == '-25299') {
          final existing = await _storage.read(key: _key);
          if (existing != null) {
            _cachedUuid = existing;
            return existing;
          }
        }
        rethrow;
      }
      _cachedUuid = newUuid;
      return newUuid;
    } on MissingPluginException {
      // Fallback for environments where plugins are not initialized (e.g., tests)
      _cachedUuid = _cachedUuid ?? _uuid.v4();
      return _cachedUuid!;
    }
  }
}
