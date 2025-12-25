package com.onair.hearit.app.playinghistory.infrastructure.scheduler;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
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
                .setConnectionPoolSize(64)           // 연결 풀 크기
                .setConnectionMinimumIdleSize(10)    // 최소 유휴 연결
                .setIdleConnectionTimeout(10000)     // 유휴 연결 타임아웃 (10초)
                .setConnectTimeout(3000)             // 연결 타임아웃 (3초)
                .setTimeout(2000)                    // 명령 타임아웃 (2초)
                .setRetryAttempts(3)                 // 재시도 횟수
                .setRetryInterval(1500);             // 재시도 간격 (1.5초)

        if (StringUtils.hasText(redisPassword)) {
            config.useSingleServer().setPassword(redisPassword);
        }

        return Redisson.create(config);
    }
}
