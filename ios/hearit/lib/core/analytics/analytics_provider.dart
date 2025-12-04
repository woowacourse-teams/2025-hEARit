import 'package:firebase_analytics/firebase_analytics.dart';
import 'package:flutter/foundation.dart';

/// Abstraction over analytics logging so it can be swapped in (e.g. Firebase).
abstract class AnalyticsLogger {
  Future<void> logEvent(
    String name, {
    Map<String, Object?> params = const {},
  });

  Future<void> setUserId(String userId);
}

class AnalyticsProvider {
  AnalyticsProvider._();

  static AnalyticsLogger _logger = const _NoopAnalyticsLogger();

  static void configure(AnalyticsLogger logger) {
    _logger = logger;
  }

  static AnalyticsLogger get logger => _logger;
}

/// Emits analytics calls to debugPrint; replace with Firebase impl in prod.
class DebugAnalyticsLogger implements AnalyticsLogger {
  const DebugAnalyticsLogger();

  @override
  Future<void> logEvent(
    String name, {
    Map<String, Object?> params = const {},
  }) async {
    debugPrint('[Analytics] $name $params');
  }

  @override
  Future<void> setUserId(String userId) async {
    debugPrint('[Analytics] setUserId: $userId');
  }
}

class _NoopAnalyticsLogger implements AnalyticsLogger {
  const _NoopAnalyticsLogger();

  @override
  Future<void> logEvent(
    String name, {
    Map<String, Object?> params = const {},
  }) async {}

  @override
  Future<void> setUserId(String userId) async {}
}

class FirebaseAnalyticsLogger implements AnalyticsLogger {
  FirebaseAnalyticsLogger(this._analytics);

  final FirebaseAnalytics _analytics;

  @override
  Future<void> logEvent(
    String name, {
    Map<String, Object?> params = const {},
  }) {
    return _analytics.logEvent(name: name, parameters: params);
  }

  @override
  Future<void> setUserId(String userId) {
    return _analytics.setUserId(id: userId);
  }
}
