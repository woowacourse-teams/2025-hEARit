package com.onair.hearit.app.playinghistory.infrastructure.buffer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onair.hearit.app.playinghistory.infrastructure.converter.PlayingHistoryConverter;
import com.onair.hearit.core.domain.PlayingHistory;
import com.onair.hearit.core.infrastructure.jdbc.PlayingHistoryCommandRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis 기반 재생 기록 저장소
 * - Redisson 분산 락으로 동시성 제어
 * - Redis Hash 사용
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlayingHistoryRedisBuffer implements PlayingHistoryBuffer {

    private static final String REDIS_HASH_KEY = "playing_history";
    private static final String LOCK_PREFIX = "lock:playing_history:";
    private static final long LOCK_WAIT_TIME = 100; // ms
    private static final long LOCK_LEASE_TIME = 3000; // ms

    private final RedisTemplate<String, String> redisTemplate;
    private final RedissonClient redissonClient;
    private final PlayingHistoryCommandRepository commandRepository;
    private final PlayingHistoryConverter converter;
    private final ObjectMapper objectMapper;

    @Override
    public void add(PlayingHistory playingHistory, long clientEventTime) {
        validateClientEventTime(clientEventTime);

        String field = buildHashField(playingHistory.getUserUuid(), playingHistory.getHearitId());
        String lockKey = LOCK_PREFIX + field;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 분산 락 획득
            boolean acquired = lock.tryLock(LOCK_WAIT_TIME, LOCK_LEASE_TIME, TimeUnit.MILLISECONDS);
            if (!acquired) {
                log.warn("재생 기록 락 획득 실패: {}", field);
                throw new RuntimeException("Failed to acquire lock: " + field);
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
            throw new RuntimeException("재생 기록 저장 중 인터럽트: " + field, e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("재생 기록 JSON 변환 실패: " + field, e);
        }
    }

    @Override
    public void flush() {
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

            // PlayValue → PlayingHistory 변환
            List<PlayingHistory> histories = converter.toPlayingHistories(playValues);

            // DB 일괄 저장
            commandRepository.bulkInsert(histories);

            // Redis에서 저장 완료된 항목 제거
            hashOps.delete(REDIS_HASH_KEY, snapshot.keySet().toArray());

            log.info("Redis 재생 기록 flush 완료: {} 건", histories.size());

        } catch (Exception e) {
            log.error("Redis 재생 기록 flush 실패 (데이터는 Redis에 유지됨)", e);
            throw new RuntimeException("Redis flush failed", e);
        }
    }

    @Override
    public int size() {
        try {
            HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
            return hashOps.size(REDIS_HASH_KEY).intValue();
        } catch (Exception e) {
            log.error("Redis 크기 조회 실패", e);
            return 0;
        }
    }

    private void validateClientEventTime(long clientEventTime) {
        if (clientEventTime <= 0) {
            throw new IllegalArgumentException("clientEventTime must be positive: " + clientEventTime);
        }
    }

    private String buildHashField(String userUuid, long hearitId) {
        return userUuid + ":" + hearitId;
    }
}
