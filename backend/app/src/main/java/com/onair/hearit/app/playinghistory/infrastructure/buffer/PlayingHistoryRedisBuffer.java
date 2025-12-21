package com.onair.hearit.app.playinghistory.infrastructure.buffer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onair.hearit.app.exception.custom.BufferRequestException;
import com.onair.hearit.app.playinghistory.infrastructure.converter.PlayingHistoryConverter;
import com.onair.hearit.core.domain.PlayingHistory;
import com.onair.hearit.core.domain.exception.PlayingHistoryDomainException;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class PlayingHistoryRedisBuffer implements PlayingHistoryBuffer {

    private static final String REDIS_HASH_KEY = "playing_history";
    private static final String LOCK_PREFIX = "lock:playing_history:";
    private static final long LOCK_WAIT_TIME = 1000; // ms
    private static final long LOCK_LEASE_TIME = 3000; // ms

    private final RedisTemplate<String, String> redisTemplate;
    private final RedissonClient redissonClient;
    private final PlayingHistoryCommandRepository commandRepository;
    private final PlayingHistoryConverter converter;
    private final ObjectMapper objectMapper;

    @Override
    public void add(PlayingHistory playingHistory, long clientEventTime) {
        String field = buildHashField(playingHistory.getUserUuid(), playingHistory.getHearitId());
        PlayHistoryValue incoming = PlayHistoryValue.from(playingHistory, clientEventTime);
        RLock lock = redissonClient.getLock(LOCK_PREFIX + field);
        acquireLockOrThrow(lock, field);
        try {
            if (shouldUpdatePlayHistory(field, incoming)) {
                savePlayHistoryToRedis(field, incoming);
            }
        } finally {
            releaseLock(lock);
        }
    }

    @Override
    public void flush() {
        if (Boolean.FALSE.equals(redisTemplate.hasKey(REDIS_HASH_KEY))) {
            return;
        }
        String snapshotKey = createSnapshotKey();
        try {
            moveToSnapshot(snapshotKey);
            Map<String, String> snapshotData = readSnapshotData(snapshotKey);
            if (snapshotData.isEmpty()) {
                deleteSnapshot(snapshotKey);
                return;
            }
            List<PlayHistoryValue> playValues = parseToPlayValues(snapshotData);
            if (playValues.isEmpty()) {
                return;
            }
            saveHistoriesToDatabase(playValues);
            deleteSnapshot(snapshotKey);
        } catch (Exception e) {
            log.error("Redis 재생 기록 flush 실패", e);
            throw new BufferRequestException("재생 기록 저장 중 오류가 발생했습니다.");
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

    private void releaseLock(RLock lock) {
        try {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        } catch (Exception e) {
            log.error("락 해제 실패", e);
        }
    }

    private String buildHashField(String userUuid, long hearitId) {
        return userUuid + ":" + hearitId;
    }

    private void acquireLockOrThrow(RLock lock, String field) {
        try {
            boolean acquired = lock.tryLock(LOCK_WAIT_TIME, LOCK_LEASE_TIME, TimeUnit.MILLISECONDS);
            if (!acquired) {
                log.warn("재생 기록 락 획득 실패: {}", field);
                throw new BufferRequestException("락 획득 실패.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BufferRequestException("재생 기록 저장 중 오류가 발생했습니다.");
        }
    }

    private boolean shouldUpdatePlayHistory(String field, PlayHistoryValue incoming) {
        try {
            HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
            String existingJson = hashOps.get(REDIS_HASH_KEY, field);
            PlayHistoryValue existing = objectMapper.readValue(existingJson, PlayHistoryValue.class);
            return !existing.isMoreRecentThan(incoming);
        } catch (JsonProcessingException e) {
            throw new BufferRequestException("재생 기록 데이터 처리 중 오류가 발생했습니다.");
        }
    }

    private void savePlayHistoryToRedis(String field, PlayHistoryValue value) {
        try {
            HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
            String valueJson = objectMapper.writeValueAsString(value);
            hashOps.put(REDIS_HASH_KEY, field, valueJson);
        } catch (JsonProcessingException e) {
            throw new BufferRequestException("재생 기록 데이터 저장 중 오류가 발생했습니다.");
        }
    }

    private String createSnapshotKey() {
        return REDIS_HASH_KEY + ":snapshot:" + System.currentTimeMillis();
    }

    private void moveToSnapshot(String snapshotKey) {
        redisTemplate.rename(REDIS_HASH_KEY, snapshotKey);
    }

    private Map<String, String> readSnapshotData(String snapshotKey) {
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        return hashOps.entries(snapshotKey);
    }

    private List<PlayHistoryValue> parseToPlayValues(Map<String, String> snapshotData) {
        List<PlayHistoryValue> playValues = new ArrayList<>();
        for (Map.Entry<String, String> entry : snapshotData.entrySet()) {
            try {
                PlayHistoryValue value = objectMapper.readValue(entry.getValue(), PlayHistoryValue.class);
                playValues.add(value);
            } catch (JsonProcessingException e) {
                log.error("재생 기록 역직렬화 실패: field={}", entry.getKey(), e);
            }
        }
        return playValues;
    }

    private void saveHistoriesToDatabase(List<PlayHistoryValue> playValues) {
        List<PlayingHistory> histories = converter.toPlayingHistories(playValues);
        commandRepository.bulkInsert(histories);
    }

    private void deleteSnapshot(String snapshotKey) {
        redisTemplate.delete(snapshotKey);
    }
}
