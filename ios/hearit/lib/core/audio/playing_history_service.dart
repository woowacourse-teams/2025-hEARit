import 'package:flutter/foundation.dart';

import '../../features/detail/playing_history_repository.dart';
import '../storage/playing_history_storage.dart';

/// 재생 기록 관리 서비스
///
/// 역할:
/// - 재생 기록 저장 로직 통합
/// - 중복 방지 (1초 이내)
/// - 로컬 큐 관리
/// - 실패한 요청 재시도
class PlayingHistoryService {
  PlayingHistoryService({
    PlayingHistoryRepository? repository,
    PlayingHistoryStorage? storage,
  }) : _repository = repository ?? PlayingHistoryRepository(),
       _storage = storage ?? PlayingHistoryStorage();

  final PlayingHistoryRepository _repository;
  final PlayingHistoryStorage _storage;

  // 중복 방지를 위한 마지막 저장 정보
  DateTime? _lastSaveTime;
  int? _lastHearitId;
  int? _lastPlayTime;

  /// 재생 기록 저장 (핵심 메서드)
  ///
  /// [hearitId]: 팟캐스트 ID
  /// [lastPlayTime]: 마지막 재생 위치 (밀리초)
  /// [reason]: 저장 이유 (디버깅용)
  Future<void> savePlayingHistory({
    required int hearitId,
    required int lastPlayTime,
    String? reason,
  }) async {
    // 1. 중복 방지 체크 (1초 이내 같은 hearitId + 같은 lastPlayTime)
    if (_shouldSkipDuplicate(hearitId, lastPlayTime)) {
      debugPrint(
        '⏭️ 재생 기록 저장 스킵 (중복): hearitId=$hearitId, '
        'lastPlayTime=${lastPlayTime}ms ${reason != null ? "($reason)" : ""}',
      );
      return;
    }

    // 2. 현재 시간 (Unix Epoch Time, 밀리초)
    final clientEventTime = DateTime.now().millisecondsSinceEpoch;

    debugPrint(
      '💾 재생 기록 저장 시도: hearitId=$hearitId, '
      'lastPlayTime=${lastPlayTime}ms, clientEventTime=$clientEventTime '
      '${reason != null ? "($reason)" : ""}',
    );

    // 3. 마지막 저장 정보 업데이트
    _updateLastSaveInfo(hearitId, lastPlayTime);

    // 4. API 호출
    try {
      await _repository.savePlayingHistory(
        hearitId: hearitId,
        lastPlayTime: lastPlayTime,
        clientEventTime: clientEventTime,
      );

      debugPrint('✅ 재생 기록 저장 성공');
    } catch (error) {
      debugPrint('⚠️ 재생 기록 저장 실패, 로컬 큐에 추가: $error');

      // 5. API 실패 시 로컬 큐에 저장
      final request = _storage.createRequest(
        hearitId: hearitId,
        lastPlayTime: lastPlayTime,
        clientEventTime: clientEventTime,
      );

      await _storage.enqueue(request);
    }
  }

  /// 실패한 요청 재시도
  ///
  /// 새 팟캐스트 재생 시 호출되어 큐에 남아있는 요청들을 재시도합니다.
  Future<void> retryFailedRequests() async {
    final pending = await _storage.getAllPending();

    if (pending.isEmpty) {
      return;
    }

    debugPrint('🔄 재생 기록 재시도 시작: ${pending.length}개');

    int successCount = 0;
    int failCount = 0;

    for (final request in pending) {
      try {
        await _repository.savePlayingHistory(
          hearitId: request.hearitId,
          lastPlayTime: request.lastPlayTime,
          clientEventTime: request.clientEventTime,
        );

        // 성공 시 큐에서 삭제
        await _storage.remove(request.id);
        successCount++;
      } catch (error) {
        // 실패 시 그대로 큐에 남김 (다음에 재시도)
        debugPrint('⚠️ 재생 기록 재시도 실패 (${request.id}): $error');
        failCount++;
      }
    }

    debugPrint('🔄 재생 기록 재시도 완료: 성공 $successCount개, 실패 $failCount개');
  }

  /// 중복 방지 체크
  ///
  /// 1초 이내에 같은 hearitId + 같은 lastPlayTime 요청은 무시
  bool _shouldSkipDuplicate(int hearitId, int lastPlayTime) {
    if (_lastHearitId == null || _lastSaveTime == null) {
      return false; // 첫 저장
    }

    final now = DateTime.now();
    final timeDiff = now.difference(_lastSaveTime!);

    // 1초 이내 + 같은 hearitId + 같은 lastPlayTime
    return timeDiff < const Duration(seconds: 1) &&
        _lastHearitId == hearitId &&
        _lastPlayTime == lastPlayTime;
  }

  /// 마지막 저장 정보 업데이트
  void _updateLastSaveInfo(int hearitId, int lastPlayTime) {
    _lastSaveTime = DateTime.now();
    _lastHearitId = hearitId;
    _lastPlayTime = lastPlayTime;
  }

  /// 큐 상태 조회 (디버깅용)
  Future<int> getQueueSize() async {
    return await _storage.getQueueSize();
  }

  /// 큐 초기화 (개발/테스트용)
  Future<void> clearQueue() async {
    await _storage.clear();
  }
}
