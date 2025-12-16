package com.onair.hearit.app.playinghistory.infrastructure.buffer.storage;

import com.onair.hearit.app.playinghistory.infrastructure.buffer.PlayingHistoryBuffer;
import com.onair.hearit.app.playinghistory.infrastructure.buffer.converter.PlayingHistoryConverter;
import com.onair.hearit.app.playinghistory.infrastructure.buffer.converter.PlayingHistoryConverter.PlayKey;
import com.onair.hearit.app.playinghistory.infrastructure.buffer.converter.PlayingHistoryConverter.PlayValue;
import com.onair.hearit.core.domain.PlayingHistory;
import com.onair.hearit.core.infrastructure.jdbc.PlayingHistoryCommandRepository;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 로컬 메모리 기반 재생 기록 저장소 (Fallback용)
 * - ConcurrentHashMap 사용
 * - Flush 실패 시 롤백 지원
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LocalPlayingHistoryStorage implements PlayingHistoryBuffer {

    private static final int MAX_SIZE = 100_000;

    private final Map<PlayKey, PlayValue> cache = new ConcurrentHashMap<>();
    private final PlayingHistoryCommandRepository commandRepository;
    private final PlayingHistoryConverter converter;

    @Override
    public void add(PlayingHistory playingHistory, long clientEventTime) {
        validateClientEventTime(clientEventTime);

        PlayKey key = new PlayKey(playingHistory.getUserUuid(), playingHistory.getHearitId());

        // 용량 체크
        if (!cache.containsKey(key) && cache.size() >= MAX_SIZE) {
            log.error("Fallback 캐시 용량 초과: {}", cache.size());
            throw new RuntimeException("Fallback cache is full: " + cache.size());
        }

        // ConcurrentHashMap.compute() - 원자적 업데이트
        cache.compute(key, (k, existing) -> {
            PlayValue incoming = PlayValue.from(playingHistory, clientEventTime);

            // 기존 데이터가 더 최신이면 유지
            if (existing != null && existing.isMoreRecentThan(incoming)) {
                return existing;
            }
            return incoming;
        });
    }

    @Override
    public void flush() {
        Map<PlayKey, PlayValue> snapshot = createSnapshotAndRemoveFromCache();
        if (snapshot.isEmpty()) {
            return;
        }

        log.info("Fallback 캐시 재생 기록 flush 시작: {} 건", snapshot.size());

        try {
            // PlayValue → PlayingHistory 변환
            List<PlayingHistory> histories = converter.toPlayingHistories(snapshot.values());

            // DB 일괄 저장
            commandRepository.bulkInsert(histories);

            log.info("Fallback 캐시 재생 기록 flush 완료: {} 건", histories.size());

        } catch (Exception e) {
            // Flush 실패 시 롤백 (다음 flush에서 재시도)
            rollbackSnapshot(snapshot);
            log.error("Fallback 캐시 flush 실패, 롤백 수행. size: {}", snapshot.size(), e);
            throw new RuntimeException("Local storage flush failed", e);
        }
    }

    @Override
    public int size() {
        return cache.size();
    }

    private Map<PlayKey, PlayValue> createSnapshotAndRemoveFromCache() {
        Map<PlayKey, PlayValue> snapshot = new ConcurrentHashMap<>();
        cache.forEach((key, value) -> {
            // CAS (Compare-And-Swap) 방식으로 원자적 제거
            if (cache.remove(key, value)) {
                snapshot.put(key, value);
            }
        });
        return snapshot;
    }

    private void rollbackSnapshot(Map<PlayKey, PlayValue> snapshot) {
        try {
            snapshot.forEach((key, newValue) ->
                    cache.merge(key, newValue, (oldValue, incomingValue) -> {
                        // 더 최신 데이터 유지
                        if (oldValue.isMoreRecentThan(incomingValue)) {
                            return oldValue;
                        }
                        return incomingValue;
                    })
            );
        } catch (Exception e) {
            log.error("롤백도 실패, 데이터 손실 가능성. size: {}", snapshot.size(), e);
            // 메트릭 기록 또는 알림 필요
        }
    }

    private void validateClientEventTime(long clientEventTime) {
        if (clientEventTime <= 0) {
            throw new IllegalArgumentException("clientEventTime must be positive: " + clientEventTime);
        }
    }
}
