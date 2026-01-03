import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:uuid/uuid.dart';

/// 재생 기록 저장 요청 데이터 모델
class PlayingHistoryRequest {
  final String id; // 요청 고유 ID (UUID)
  final int hearitId;
  final int lastPlayTime;
  final int clientEventTime;
  final DateTime createdAt; // 큐에 추가된 시간

  PlayingHistoryRequest({
    required this.id,
    required this.hearitId,
    required this.lastPlayTime,
    required this.clientEventTime,
    required this.createdAt,
  });

  /// JSON으로 직렬화
  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'hearitId': hearitId,
      'lastPlayTime': lastPlayTime,
      'clientEventTime': clientEventTime,
      'createdAt': createdAt.millisecondsSinceEpoch,
    };
  }

  /// JSON에서 역직렬화
  factory PlayingHistoryRequest.fromJson(Map<String, dynamic> json) {
    return PlayingHistoryRequest(
      id: json['id'] as String,
      hearitId: json['hearitId'] as int,
      lastPlayTime: json['lastPlayTime'] as int,
      clientEventTime: json['clientEventTime'] as int,
      createdAt: DateTime.fromMillisecondsSinceEpoch(json['createdAt'] as int),
    );
  }

  /// 7일 이상 오래된 요청인지 확인
  bool get isExpired {
    final now = DateTime.now();
    final difference = now.difference(createdAt);
    return difference.inDays >= 7;
  }
}

/// 재생 기록 로컬 큐 관리 (SharedPreferences)
class PlayingHistoryStorage {
  PlayingHistoryStorage({SharedPreferences? preferences})
    : _preferences = preferences;

  SharedPreferences? _preferences;
  static const String _keyPrefix = 'playing_history_queue';
  static const int _maxQueueSize = 100; // 최대 큐 크기
  final _uuid = const Uuid();

  /// SharedPreferences 초기화
  Future<void> _ensureInitialized() async {
    _preferences ??= await SharedPreferences.getInstance();
  }

  /// 실패한 요청을 큐에 추가
  Future<void> enqueue(PlayingHistoryRequest request) async {
    await _ensureInitialized();

    try {
      // 현재 큐 가져오기
      final queue = await getAllPending();

      // 큐 크기 제한 체크
      if (queue.length >= _maxQueueSize) {
        debugPrint('⚠️ 재생 기록 큐가 가득 참 (${queue.length}개). 가장 오래된 항목 삭제.');
        // 가장 오래된 항목 삭제
        final oldest = queue.first;
        await remove(oldest.id);
      }

      // 큐에 추가
      final key = '$_keyPrefix:${request.id}';
      final json = jsonEncode(request.toJson());
      await _preferences!.setString(key, json);

      debugPrint(
        '📦 재생 기록 큐에 추가: ${request.id} (hearitId: ${request.hearitId})',
      );
    } catch (e) {
      debugPrint('❌ 재생 기록 큐 추가 실패: $e');
    }
  }

  /// 큐에서 모든 대기 중인 요청 가져오기
  Future<List<PlayingHistoryRequest>> getAllPending() async {
    await _ensureInitialized();

    try {
      final keys = _preferences!.getKeys().where(
        (key) => key.startsWith(_keyPrefix),
      );
      final requests = <PlayingHistoryRequest>[];

      for (final key in keys) {
        final json = _preferences!.getString(key);
        if (json != null) {
          try {
            final data = jsonDecode(json) as Map<String, dynamic>;
            final request = PlayingHistoryRequest.fromJson(data);

            // 7일 이상 오래된 요청은 자동 삭제
            if (request.isExpired) {
              debugPrint(
                '🗑️ 오래된 재생 기록 삭제: ${request.id} (${request.createdAt})',
              );
              await remove(request.id);
            } else {
              requests.add(request);
            }
          } catch (e) {
            debugPrint('⚠️ 재생 기록 파싱 실패: $key - $e');
            // 파싱 실패한 항목 삭제
            await _preferences!.remove(key);
          }
        }
      }

      // 생성 시간 순으로 정렬 (오래된 것부터)
      requests.sort((a, b) => a.createdAt.compareTo(b.createdAt));

      if (requests.isNotEmpty) {
        debugPrint('📦 대기 중인 재생 기록: ${requests.length}개');
      }

      return requests;
    } catch (e) {
      debugPrint('❌ 재생 기록 큐 조회 실패: $e');
      return [];
    }
  }

  /// 성공한 요청을 큐에서 삭제
  Future<void> remove(String requestId) async {
    await _ensureInitialized();

    try {
      final key = '$_keyPrefix:$requestId';
      await _preferences!.remove(key);
      debugPrint('✅ 재생 기록 큐에서 삭제: $requestId');
    } catch (e) {
      debugPrint('❌ 재생 기록 큐 삭제 실패: $e');
    }
  }

  /// 전체 큐 초기화 (개발/테스트용)
  Future<void> clear() async {
    await _ensureInitialized();

    try {
      final keys = _preferences!.getKeys().where(
        (key) => key.startsWith(_keyPrefix),
      );
      for (final key in keys) {
        await _preferences!.remove(key);
      }
      debugPrint('🗑️ 재생 기록 큐 전체 초기화');
    } catch (e) {
      debugPrint('❌ 재생 기록 큐 초기화 실패: $e');
    }
  }

  /// 새로운 요청 생성 (헬퍼 메서드)
  PlayingHistoryRequest createRequest({
    required int hearitId,
    required int lastPlayTime,
    required int clientEventTime,
  }) {
    return PlayingHistoryRequest(
      id: _uuid.v4(),
      hearitId: hearitId,
      lastPlayTime: lastPlayTime,
      clientEventTime: clientEventTime,
      createdAt: DateTime.now(),
    );
  }

  /// 큐 크기 가져오기
  Future<int> getQueueSize() async {
    final queue = await getAllPending();
    return queue.length;
  }
}
