package com.onair.hearit.app.playinghistory.infrastructure.buffer.event;

import com.onair.hearit.app.playinghistory.infrastructure.buffer.PlayingHistoryMapBuffer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Circuit Breaker가 닫힐 때(Redis 복구) 발행되는 이벤트를 비동기로 처리.
 * <p>Map Buffer에 누적된 데이터를 DB로 flush한다.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisRecoveredEventListener {

    private final PlayingHistoryMapBuffer mapBuffer;

    @EventListener
    @Async("circuitBreakerEventExecutor")
    public void handleRedisRecovered(RedisRecoveredEvent event) {
        log.debug("Redis 복구 이벤트 수신, Map Buffer 비동기 flush 시작");
        try {
            int size = mapBuffer.size();
            if (size > 0) {
                mapBuffer.flush();
                log.info("Map Buffer flush 완료 ({}건 처리)", size);
            } else {
                log.info("Map Buffer가 비어있어 flush 스킵");
            }
        } catch (Exception e) {
            log.error("Map Buffer 비동기 flush 실패, 다음 주기에 스케줄러가 재시도 예정", e);
        }
    }
}
