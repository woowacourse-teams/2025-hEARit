package com.onair.hearit.app.playinghistory.infrastructure.buffer;

import com.onair.hearit.core.domain.PlayingHistory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Slf4j
@Primary
@Component
@RequiredArgsConstructor
public class PlayingHistoryBufferFacade implements PlayingHistoryBuffer {

    private final PlayingHistoryRedisBuffer primaryStorage;
    private final PlayingHistoryMapBuffer fallbackStorage;

    @Override
    public void add(PlayingHistory playingHistory, long clientEventTime) {
        try {
            primaryStorage.add(playingHistory, clientEventTime);
        } catch (Exception e) {
            log.warn("Primary storage 실패, Fallback으로 전환. userUuid={}, hearitId={}",
                    playingHistory.getUserUuid(), playingHistory.getHearitId());
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
        return primaryStorage.size() + fallbackStorage.size();
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
        return new BufferStatus(
                primaryStorage.size(),
                fallbackStorage.size()
        );
    }

    public record BufferStatus(int primarySize, int fallbackSize) {
    }
}
