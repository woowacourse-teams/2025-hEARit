package com.onair.hearit.app.playinghistory.infrastructure.buffer.config;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;

import com.onair.hearit.app.playinghistory.infrastructure.buffer.event.RedisRecoveredEvent;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreaker.State;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Redis 재생 기록 버퍼용 Circuit Breaker 이벤트 설정.
 *
 * <p>Redis 장애 시 Map Buffer로 전환하고,
 * Redis 복구(CLOSED 전환) 시 비동기 이벤트를 발행하여 Map Buffer를 flush한다.</p>
 *
 * <p>Circuit Breaker 자체 설정은 application properties에서 관리한다.</p>
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class CircuitBreakerConfig {

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final ApplicationEventPublisher eventPublisher;


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

                    // HALF_OPEN -> CLOSED 전환 시 Redis 복구로 간주
                    if (from == State.HALF_OPEN && to == State.CLOSED) {
                        log.info("Redis 복구 감지 ({}->CLOSED), 비동기 이벤트 발행", from);
                        eventPublisher.publishEvent(new RedisRecoveredEvent());
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
