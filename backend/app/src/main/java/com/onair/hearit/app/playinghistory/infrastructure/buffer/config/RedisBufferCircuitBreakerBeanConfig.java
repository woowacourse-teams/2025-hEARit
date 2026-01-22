package com.onair.hearit.app.playinghistory.infrastructure.buffer.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisBufferCircuitBreakerBeanConfig {

    @Bean
    public CircuitBreaker redisBufferCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("redisBuffer");
    }
}
