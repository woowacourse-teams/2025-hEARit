package com.onair.hearit.app.playinghistory.infrastructure.buffer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.PlayingHistory;
import com.onair.hearit.core.infrastructure.jdbc.PlayingHistoryCommandRepository;
import com.onair.hearit.core.infrastructure.jpa.HearitRepository;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Redis 기반 Playing History 버퍼 (Write-Back 패턴)
 * - 재생 기록을 Redis Hash에 임시 저장
 * - 3초마다 일괄 DB 동기화
 * - Redisson 분산 락으로 동시성 제어
 * - Redis 실패 시 로컬 메모리 맵으로 Fallback
 */
@Slf4j
@Primary
@Component
@RequiredArgsConstructor
public class PlayingHistoryRedisBuffer implements PlayingHistoryBuffer {

    private static final String REDIS_HASH_KEY = "playing_history";
    private static final String LOCK_PREFIX = "lock:playing_history:";
    private static final long LOCK_WAIT_TIME = 100; // ms
    private static final long LOCK_LEASE_TIME = 3000; // ms

    private final RedisTemplate<String, String> redisTemplate;
    private final RedissonClient redissonClient;
    private final PlayingHistoryCommandRepository playingHistoryCommandRepository;
    private final HearitRepository hearitRepository;
    private final ObjectMapper objectMapper;

    // Fallback용 로컬 메모리 맵
    private final Map<PlayKey, PlayValue> fallbackCache = new ConcurrentHashMap<>();

    @Override
    public void add(PlayingHistory playingHistory, long clientEventTime) {
        try {
            addToRedis(playingHistory, clientEventTime);
        } catch (Exception e) {
            // Redis 실패 시 로컬 메모리 맵으로 Fallback
            log.warn("Redis 저장 실패, 로컬 메모리 맵으로 전환. userUuid={}, hearitId={}",
                    playingHistory.getUserUuid(), playingHistory.getHearitId(), e);
            addToFallbackCache(playingHistory, clientEventTime);
        }
    }

    private void addToRedis(PlayingHistory playingHistory, long clientEventTime) {
        String field = buildHashField(playingHistory.getUserUuid(), playingHistory.getHearitId());
        String lockKey = LOCK_PREFIX + field;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 분산 락 획득 (최대 100ms 대기, 3초 후 자동 해제)
            boolean acquired = lock.tryLock(LOCK_WAIT_TIME, LOCK_LEASE_TIME, TimeUnit.MILLISECONDS);
            if (!acquired) {
                log.warn("재생 기록 락 획득 실패: {}", field);
                return;
            }

            try {
                PlayValue incoming = PlayValue.from(playingHistory, clientEventTime);
                HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();

                // 기존 데이터 조회
                String existingJson = hashOps.get(REDIS_HASH_KEY, field);
                if (existingJson != null) {
                    PlayValue existing = objectMapper.readValue(existingJson, PlayValue.class);
                    // clientEventTime이 더 최신인 경우에만 업데이트
                    if (existing.isMoreRecentThan(incoming)) {
                        return;
                    }
                }

                // Redis에 저장
                String valueJson = objectMapper.writeValueAsString(incoming);
                hashOps.put(REDIS_HASH_KEY, field, valueJson);

            } finally {
                lock.unlock();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("재생 기록 저장 중 인터럽트 발생: " + field, e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("재생 기록 JSON 변환 실패: " + field, e);
        }
    }

    private void addToFallbackCache(PlayingHistory playingHistory, long clientEventTime) {
        PlayKey key = new PlayKey(playingHistory.getUserUuid(), playingHistory.getHearitId());
        fallbackCache.compute(key, (k, existing) -> {
            PlayValue incoming = PlayValue.from(playingHistory, clientEventTime);
            if (existing != null && existing.isMoreRecentThan(incoming)) {
                return existing;
            }
            return incoming;
        });
    }

    @Override
    @Transactional
    @Scheduled(fixedDelay = 3000) // 3초마다 실행
    public void flush() {
        // Redis flush 시도
        flushRedis();

        // Fallback 캐시 flush
        flushFallbackCache();
    }

    private void flushRedis() {
        try {
            HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
            Map<String, String> snapshot = hashOps.entries(REDIS_HASH_KEY);

            if (snapshot.isEmpty()) {
                return;
            }

            log.info("Redis 재생 기록 flush 시작: {} 건", snapshot.size());

            // Redis 데이터 → PlayValue 변환
            List<PlayValue> playValues = new ArrayList<>();
            for (Map.Entry<String, String> entry : snapshot.entrySet()) {
                try {
                    PlayValue value = objectMapper.readValue(entry.getValue(), PlayValue.class);
                    playValues.add(value);
                } catch (JsonProcessingException e) {
                    log.error("재생 기록 역직렬화 실패: field={}", entry.getKey(), e);
                }
            }

            if (playValues.isEmpty()) {
                return;
            }

            // Hearit 엔티티 조회 (Batch)
            List<PlayingHistory> histories = buildHistoriesFromPlayValues(playValues);

            // DB 일괄 저장
            playingHistoryCommandRepository.bulkInsert(histories);

            // Redis에서 저장 완료된 항목 제거
            hashOps.delete(REDIS_HASH_KEY, snapshot.keySet().toArray());

            log.info("Redis 재생 기록 flush 완료: {} 건", histories.size());

        } catch (Exception e) {
            log.error("Redis 재생 기록 flush 실패 (데이터는 Redis에 유지됨)", e);
        }
    }

    private void flushFallbackCache() {
        Map<PlayKey, PlayValue> snapshot = createSnapshotAndRemoveFromCache();
        if (snapshot.isEmpty()) {
            return;
        }

        log.info("Fallback 캐시 재생 기록 flush 시작: {} 건", snapshot.size());

        try {
            List<PlayingHistory> histories = buildHistoriesFromSnapshot(snapshot);
            playingHistoryCommandRepository.bulkInsert(histories);
            log.info("Fallback 캐시 재생 기록 flush 완료: {} 건", histories.size());
        } catch (Exception e) {
            rollbackSnapshot(snapshot);
            log.error("Fallback 캐시 재생 기록 flush 실패, 롤백 수행. snapshot size: {}", snapshot.size(), e);
        }
    }

    private Map<PlayKey, PlayValue> createSnapshotAndRemoveFromCache() {
        Map<PlayKey, PlayValue> snapshot = new ConcurrentHashMap<>();
        fallbackCache.forEach((key, value) -> {
            if (fallbackCache.remove(key, value)) {
                snapshot.put(key, value);
            }
        });
        return snapshot;
    }

    private List<PlayingHistory> buildHistoriesFromSnapshot(Map<PlayKey, PlayValue> snapshot) {
        Set<Long> hearitIds = snapshot.values().stream()
                .map(PlayValue::hearitId)
                .collect(Collectors.toSet());
        Map<Long, Hearit> hearitMap = hearitRepository.findAllById(hearitIds)
                .stream()
                .collect(Collectors.toMap(Hearit::getId, h -> h));
        return snapshot.values().stream().map(v -> new PlayingHistory(
                        v.userUuid(),
                        hearitMap.get(v.hearitId()),
                        v.lastPlayTime()
                ))
                .toList();
    }

    private void rollbackSnapshot(Map<PlayKey, PlayValue> snapshot) {
        snapshot.forEach((key, newValue) ->
                fallbackCache.merge(key, newValue, (oldValue, incomingValue) -> {
                    if (oldValue.isMoreRecentThan(incomingValue)) {
                        return oldValue;
                    }
                    return incomingValue;
                })
        );
    }

    private List<PlayingHistory> buildHistoriesFromPlayValues(List<PlayValue> playValues) {
        Set<Long> hearitIds = playValues.stream()
                .map(PlayValue::hearitId)
                .collect(Collectors.toSet());

        Map<Long, Hearit> hearitMap = hearitRepository.findAllById(hearitIds)
                .stream()
                .collect(Collectors.toMap(Hearit::getId, h -> h));

        return playValues.stream()
                .map(v -> new PlayingHistory(
                        v.userUuid(),
                        hearitMap.get(v.hearitId()),
                        v.lastPlayTime()
                ))
                .toList();
    }

    private String buildHashField(String userUuid, long hearitId) {
        return userUuid + ":" + hearitId;
    }

    @PreDestroy
    public void shutdown() {
        log.info("애플리케이션 종료 시 재생 기록 flush 시작");
        flush();
    }

    // Fallback 캐시용 키 객체
    private record PlayKey(String userUuid, long hearitId) {
    }

    // Redis에 저장되는 재생 기록 값 객체
    private record PlayValue(String userUuid, long hearitId, long lastPlayTime, long clientEventTime) {

        static PlayValue from(PlayingHistory history, long clientEventTime) {
            return new PlayValue(
                    history.getUserUuid(),
                    history.getHearitId(),
                    history.getLastPlayTime(),
                    clientEventTime
            );
        }

        boolean isMoreRecentThan(PlayValue other) {
            return other != null && this.clientEventTime > other.clientEventTime();
        }
    }
}
