package com.onair.hearit.app.playinghistory.infrastructure.buffer.config;

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

    @Value("${redisson.timeout}")
    private int timeout;

    @Value("${redisson.connect-timeout}")
    private int connectTimeout;

    @Value("${redisson.retry-attempts}")
    private int retryAttempts;

    @Value("${redisson.retry-interval}")
    private int retryInterval;

    private static final String REDIS_URL_PREFIX = "redis://";

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress(REDIS_URL_PREFIX + redisHost + ":" + redisPort)
                .setConnectionPoolSize(64)           // 연결 풀 크기
                .setConnectionMinimumIdleSize(10)    // 최소 유휴 연결
                .setIdleConnectionTimeout(10000)     // 유휴 연결 타임아웃 (10초)
                .setConnectTimeout(connectTimeout)   // 연결 타임아웃 (설정 가능)
                .setTimeout(timeout)                 // 명령 타임아웃 (설정 가능)
                .setRetryAttempts(retryAttempts)     // 재시도 횟수 (설정 가능)
                .setRetryInterval(retryInterval);    // 재시도 간격 (설정 가능)

        if (StringUtils.hasText(redisPassword)) {
            config.useSingleServer().setPassword(redisPassword);
        }

        return Redisson.create(config);
    }
}
