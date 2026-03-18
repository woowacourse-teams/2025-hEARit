package com.onair.hearit.app.playinghistory.infrastructure.buffer.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Circuit Breaker 이벤트 처리 등 비동기 작업을 위한 ThreadPool 설정
 */
@Configuration
@EnableAsync
public class playingHistoryAsyncConfig {

    @Bean(name = "circuitBreakerEventExecutor")
    public Executor circuitBreakerEventExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);           // 기본 스레드 수
        executor.setMaxPoolSize(5);            // 최대 스레드 수
        executor.setQueueCapacity(100);        // 큐 크기
        executor.setThreadNamePrefix("circuit-event-");
        executor.setWaitForTasksToCompleteOnShutdown(true);  // 종료 시 작업 완료 대기
        executor.setAwaitTerminationSeconds(60);             // 최대 60초 대기
        executor.initialize();
        return executor;
    }
}
