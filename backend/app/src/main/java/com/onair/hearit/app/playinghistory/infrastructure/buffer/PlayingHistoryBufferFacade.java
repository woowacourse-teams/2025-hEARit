package com.onair.hearit.app.playinghistory.infrastructure.buffer;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.onair.hearit.app.exception.custom.RedisBufferException;
import com.onair.hearit.core.domain.PlayingHistory;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Primary
@Component
@RequiredArgsConstructor
public class PlayingHistoryBufferFacade implements PlayingHistoryBuffer {

    private final PlayingHistoryRedisBufferWithCircuitBreaker primaryStorage;
    private final PlayingHistoryMapBuffer fallbackStorage;

    @Override
    public void add(PlayingHistory playingHistory, long clientEventTime) {
        try {
            primaryStorage.add(playingHistory, clientEventTime);
        } catch (CallNotPermittedException e) {
            log.debug("Circuit OPEN, Fallback 사용. userUuid={}, hearitId={}",
                    playingHistory.getUserUuid(), playingHistory.getHearitId());
            fallbackStorage.add(playingHistory, clientEventTime);
        } catch (RedisBufferException e) {
            log.warn("Redis 인프라 실패, Fallback으로 전환. userUuid={}, hearitId={}, error={}",
                    playingHistory.getUserUuid(), playingHistory.getHearitId(), e.getMessage());
            try {
                fallbackStorage.add(playingHistory, clientEventTime);
            } catch (Exception fallbackException) {
                log.error("Fallback storage도 실패. userUuid={}, hearitId={}",
                        playingHistory.getUserUuid(), playingHistory.getHearitId(), fallbackException);
            }
        }
    }

    @Override
    public void flush() {
        flushStorage("Primary", primaryStorage);
        flushStorage("Fallback", fallbackStorage);
    }

    @Override
    public int size() {
        int primarySize = getSizeOrZero(primaryStorage);
        int fallbackSize = getSizeOrZero(fallbackStorage);
        return primarySize + fallbackSize;
    }

    private int getSizeOrZero(PlayingHistoryBuffer storage) {
        try {
            return storage.size();
        } catch (Exception e) {
            log.error("Storage 크기 조회 실패", e);
            return 0;
        }
    }

    private void flushStorage(String storageName, PlayingHistoryBuffer storage) {
        try {
            int size = storage.size();
            if (size > 0) {
                storage.flush();
            }
        } catch (Exception e) {
            log.error("{} flush 실패 ", storageName, e);
        }
    }

    public BufferStatus getStatus() {
        int primarySize = getSizeOrZero(primaryStorage);
        int fallbackSize = getSizeOrZero(fallbackStorage);
        return new BufferStatus(primarySize, fallbackSize);
    }

    public record BufferStatus(int primarySize, int fallbackSize) {
    }
}
