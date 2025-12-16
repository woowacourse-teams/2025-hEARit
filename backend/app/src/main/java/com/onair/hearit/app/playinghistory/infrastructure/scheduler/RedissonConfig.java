package com.onair.hearit.app.playinghistory.infrastructure.scheduler;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson 분산 락 설정
 * - Playing History Redis Write-Back 패턴에서 동시성 제어를 위해 사용
 */
@Configuration
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
        name = "hearit.playing-history.buffer-type",
        havingValue = "redis"
)
public class RedissonConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.password}")
    private String redisPassword;

    private static final String REDIS_URL_PREFIX = "redis://";

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress(REDIS_URL_PREFIX + redisHost + ":" + redisPort)
                .setPassword(redisPassword)
                .setConnectionPoolSize(64)           // 연결 풀 크기
                .setConnectionMinimumIdleSize(10)    // 최소 유휴 연결
                .setIdleConnectionTimeout(10000)     // 유휴 연결 타임아웃 (10초)
                .setConnectTimeout(3000)             // 연결 타임아웃 (3초)
                .setTimeout(2000)                    // 명령 타임아웃 (2초)
                .setRetryAttempts(3)                 // 재시도 횟수
                .setRetryInterval(1500);             // 재시도 간격 (1.5초)

        return Redisson.create(config);
    }
}
