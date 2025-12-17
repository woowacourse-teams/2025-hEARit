package com.onair.hearit.app.playinghistory.infrastructure.buffer;

import com.onair.hearit.core.domain.PlayingHistory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * 재생 기록 버퍼 Facade
 * - Primary Storage (Redis)와 Fallback Storage (Local Memory)를 조합
 * - Primary 실패 시 자동으로 Fallback 사용
 */
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
            // 1차: Redis 저장 시도
            primaryStorage.add(playingHistory, clientEventTime);
        } catch (Exception e) {
            // 2차: Fallback 로컬 메모리 저장
            log.warn("Primary storage 실패, Fallback으로 전환. userUuid={}, hearitId={}",
                    playingHistory.getUserUuid(), playingHistory.getHearitId(), e);
            try {
                fallbackStorage.add(playingHistory, clientEventTime);
            } catch (Exception fallbackException) {
                // 3차: 로깅 (서비스 중단 방지)
                log.error("Fallback storage도 실패. userUuid={}, hearitId={}",
                        playingHistory.getUserUuid(), playingHistory.getHearitId(), fallbackException);
            }
        }
    }

    @Override
    public void flush() {
        // Primary flush
        flushStorage("Primary (Redis)", primaryStorage);
        // Fallback flush
        flushStorage("Fallback (Local)", fallbackStorage);
    }

    @Override
    public int size() {
        return primaryStorage.size() + fallbackStorage.size();
    }

    private void flushStorage(String storageName, PlayingHistoryBuffer storage) {
        try {
            int size = storage.size();
            if (size > 0) {
                log.debug("{}flush 시작: {} 건", storageName, size);
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
