package com.onair.hearit.app.playinghistory.infrastructure.buffer;

import java.time.Duration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

/**
 * 테스트용 Circuit Breaker Registry 설정
 *
 * <p>DataJpaTest는 슬라이스 테스트라서 resilience4j 자동 설정이 로드되지 않으므로,
 * 수동으로 CircuitBreakerRegistry를 생성해야 함.</p>
 */
@TestConfiguration
public class TestCircuitBreakerConfig {

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowSize(10)
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .permittedNumberOfCallsInHalfOpenState(3)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .slowCallRateThreshold(100)
                .slowCallDurationThreshold(Duration.ofSeconds(2))
                .build();

        return CircuitBreakerRegistry.of(config);
    }
}
