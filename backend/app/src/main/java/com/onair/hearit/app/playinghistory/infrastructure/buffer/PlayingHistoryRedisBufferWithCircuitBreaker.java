package com.onair.hearit.app.playinghistory.infrastructure.buffer;

import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import com.onair.hearit.core.domain.PlayingHistory;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Redis Buffer에 Circuit Breaker를 적용한 Wrapper.
 *
 * <p>add()만 Circuit으로 보호하며,
 * flush/size는 관리성 작업이므로 Circuit 상태와 무관하게 실행됨.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlayingHistoryRedisBufferWithCircuitBreaker implements PlayingHistoryBuffer {

    private final PlayingHistoryRedisBuffer delegate;
    private final CircuitBreaker redisBufferCircuitBreaker;

    @Override
    public void add(PlayingHistory playingHistory, long clientEventTime) {
        Supplier<Void> supplier = () -> {
            delegate.add(playingHistory, clientEventTime);
            return null;
        };
        redisBufferCircuitBreaker.executeSupplier(supplier);
    }

    @Override
    public void flush() {
        delegate.flush();
    }

    @Override
    public int size() {
        return delegate.size();
    }
}
