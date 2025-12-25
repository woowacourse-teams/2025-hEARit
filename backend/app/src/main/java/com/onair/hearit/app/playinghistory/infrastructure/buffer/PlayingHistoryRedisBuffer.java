package com.onair.hearit.app.playinghistory.infrastructure.buffer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onair.hearit.app.exception.custom.BufferRequestException;
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
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlayingHistoryRedisBuffer implements PlayingHistoryBuffer {

    private static final String REDIS_HASH_KEY = "playing_history";
    private static final String REDIS_TEMP_KEY = "playing_history:flushing";
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
        addWithLock(field, incoming);
    }

    @Override
    public void flush() {
        if (!Boolean.TRUE.equals(redisTemplate.hasKey(REDIS_HASH_KEY))) {
            log.debug("Redis flush: 키 없음");
            return;
        }
        try {
            if (Boolean.TRUE.equals(redisTemplate.hasKey(REDIS_TEMP_KEY))) {
                log.warn("임시 키가 이미 존재함 (이전 flush 실패?), 복원 시도");
                restoreSnapshot();
            }
            moveToSnapshot();
            HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
            Map<String, String> snapshotData = hashOps.entries(REDIS_TEMP_KEY);
            if (snapshotData.isEmpty()) {
                redisTemplate.delete(REDIS_TEMP_KEY);
                log.debug("Redis flush: 스냅샷 비어있음, 키 삭제");
                return;
            }
            List<PlayHistoryValue> playValues = parseToPlayValues(snapshotData);
            if (playValues.isEmpty()) {
                redisTemplate.delete(REDIS_TEMP_KEY);
                log.warn("Redis flush: 모든 데이터 파싱 실패, 키 삭제");
                return;
            }
            saveHistoriesToDatabase(playValues);
            redisTemplate.delete(REDIS_TEMP_KEY);
            log.info("Redis 재생 기록 flush 완료: {} 건", playValues.size());
        } catch (DataAccessException e) {
            log.error("Redis 재생 기록 flush 실패 - Redis 오류", e);
            restoreSnapshot();
        } catch (Exception e) {
            log.error("Redis 재생 기록 flush 실패 - 예상치 못한 오류", e);
            restoreSnapshot();
        }
    }

    @Override
    public int size() {
        try {
            HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
            Long size = hashOps.size(REDIS_HASH_KEY);
            if (size == null) {
                return 0;
            }
            return size.intValue();
        } catch (DataAccessException e) {
            log.error("Redis 크기 조회 실패 - Redis 오류", e);
            return 0;
        } catch (Exception e) {
            log.error("Redis 크기 조회 실패", e);
            return 0;
        }
    }

    private void addWithLock(String field, PlayHistoryValue incoming) {
        RLock lock = redissonClient.getLock(LOCK_PREFIX + field);
        try {
            if (!tryAcquireLock(lock, field)) {
                return;
            }
            try {
                saveIfNewer(field, incoming);
            } finally {
                releaseLock(lock);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("재생 기록 저장 중 인터럽트: {}", field, e);
            throw new BufferRequestException("재생 기록 저장 중 오류가 발생했습니다.");
        } catch (BufferRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("재생 기록 저장 실패: {}", field, e);
            throw new BufferRequestException("재생 기록 저장 중 오류가 발생했습니다.");
        }
    }

    private boolean tryAcquireLock(RLock lock, String field) throws InterruptedException {
        boolean acquired = lock.tryLock(LOCK_WAIT_TIME, LOCK_LEASE_TIME, TimeUnit.MILLISECONDS);
        if (!acquired) {
            log.warn("재생 기록 락 획득 실패, 요청 무시: {}", field);
        }
        return acquired;
    }

    private void saveIfNewer(String field, PlayHistoryValue incoming) {
        if (shouldUpdatePlayHistory(field, incoming)) {
            savePlayHistoryToRedis(field, incoming);
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

    private boolean shouldUpdatePlayHistory(String field, PlayHistoryValue incoming) {
        try {
            HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
            String existingJson = hashOps.get(REDIS_HASH_KEY, field);
            if (existingJson == null) {
                return true;
            }
            PlayHistoryValue existing = objectMapper.readValue(existingJson, PlayHistoryValue.class);
            return !existing.isMoreRecentThan(incoming);
        } catch (JsonProcessingException e) {
            log.error("재생 기록 파싱 실패: field={}", field, e);
            return true;
        }
    }

    private void savePlayHistoryToRedis(String field, PlayHistoryValue value) {
        try {
            HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
            String valueJson = objectMapper.writeValueAsString(value);
            hashOps.put(REDIS_HASH_KEY, field, valueJson);
        } catch (JsonProcessingException e) {
            log.error("재생 기록 직렬화 실패: field={}", field, e);
            throw new BufferRequestException("재생 기록 데이터 저장 중 오류가 발생했습니다.");
        }
    }

    private void moveToSnapshot() {
        try {
            redisTemplate.rename(REDIS_HASH_KEY, REDIS_TEMP_KEY);
        } catch (DataAccessException e) {
            log.debug("RENAME 실패: 원본 키 없음");
        }
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

    private void restoreSnapshot() {
        try {
            if (Boolean.TRUE.equals(redisTemplate.hasKey(REDIS_TEMP_KEY))) {
                HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
                Map<String, String> tempData = hashOps.entries(REDIS_TEMP_KEY);

                if (!tempData.isEmpty()) {
                    hashOps.putAll(REDIS_HASH_KEY, tempData);
                    redisTemplate.delete(REDIS_TEMP_KEY);
                    log.info("Redis 데이터 복원 완료: {} 건", tempData.size());
                }
            }
        } catch (Exception e) {
            log.error("Redis 데이터 복원 실패 - 데이터 손실 가능성!", e);
        }
    }
}
