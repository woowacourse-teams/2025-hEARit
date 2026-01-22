package com.onair.hearit.app.playinghistory.infrastructure.config;

import com.onair.hearit.app.playinghistory.infrastructure.buffer.PlayingHistoryMapBuffer;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreaker.State;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * Redis 재생 기록 버퍼용 Circuit Breaker 이벤트 설정.
 *
 * <p>Redis 장애 시 Map Buffer로 전환하고,
 * Redis 복구(CLOSED 전환) 시 Map Buffer를 flush한다.</p>
 *
 * <p>Circuit Breaker 자체 설정은 application properties에서 관리한다.</p>
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class CircuitBreakerConfig {

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final PlayingHistoryMapBuffer mapBuffer;


    @PostConstruct
    public void configureRedisBufferCircuitBreaker() {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("redisBuffer");
        registerEventListeners(circuitBreaker);
    }

    private void registerEventListeners(CircuitBreaker circuitBreaker) {
        circuitBreaker.getEventPublisher()
                .onStateTransition(event -> {
                    CircuitBreaker.State from = event.getStateTransition().getFromState();
                    CircuitBreaker.State to = event.getStateTransition().getToState();

                    log.info("Circuit 상태 전환: {} -> {}", from, to);

                    if (from == State.OPEN && to == State.CLOSED) {
                        log.info("Redis 복구 감지, Map Buffer flush 시도");
                        try {
                            mapBuffer.flush();
                            log.info("Map Buffer flush 성공");
                        } catch (Exception e) {
                            log.error("Map Buffer flush 실패, 다음 주기에 재시도 예정", e);
                        }
                    }
                });

        circuitBreaker.getEventPublisher()
                .onError(event -> {
                    log.debug("Circuit error: {}", event.getThrowable().toString());
                });

        circuitBreaker.getEventPublisher()
                .onCallNotPermitted(event -> {
                    log.debug("Circuit OPEN 상태로 Redis 호출 차단됨, Map Buffer 사용");
                });
    }
}
