package com.onair.hearit.app.hearit.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.GenericContainer;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ViewCountRateLimitScriptConfig.class)
public class ViewCountRateLimiterTest {

    static GenericContainer<?> redisContainer;
    static RedisTemplate<String, String> redisTemplate;

    ViewCountRateLimiter rateLimiter;

    @Autowired
    DefaultRedisScript<Long> viewCountLimitScript;

    @BeforeAll
    static void startRedis() {
        redisContainer = new GenericContainer<>("redis:7.2-alpine")
                .withExposedPorts(6379);
        redisContainer.start();

        RedisStandaloneConfiguration config =
                new RedisStandaloneConfiguration(
                        redisContainer.getHost(),
                        redisContainer.getFirstMappedPort()
                );

        LettuceConnectionFactory connectionFactory =
                new LettuceConnectionFactory(config);
        connectionFactory.afterPropertiesSet();

        redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);

        StringRedisSerializer serializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(serializer);
        redisTemplate.setValueSerializer(serializer);
        redisTemplate.afterPropertiesSet();
    }

    @BeforeEach
    void setUp() {
        redisTemplate.getConnectionFactory()
                .getConnection()
                .serverCommands()
                .flushAll();

        rateLimiter = new ViewCountRateLimiter(
                redisTemplate,
                viewCountLimitScript
        );
    }

    @AfterAll
    static void stopRedis() {
        redisContainer.stop();
    }

    @Test
    @DisplayName("조회수 중복 키에 대해 첫 요청은 조회수 증가를 허용하고 키를 획득한다.")
    void firstRequest_allowed() {
        // given
        UUID userUuid = UUID.randomUUID();
        Long hearitId = 1L;

        // when
        boolean isAcquired = rateLimiter.tryAcquireViewKey(userUuid, hearitId);

        // then
        assertThat(isAcquired).isTrue();
    }

    @Test
    @DisplayName("같은 사용자 + 같은 Hearit에 대한 조회수 중복 요청은 차단된다.")
    void duplicatedRequest_denied() {
        // given
        UUID userId = UUID.randomUUID();
        Long hearitId = 1L;

        // when
        boolean first = rateLimiter.tryAcquireViewKey(userId, hearitId);
        boolean second = rateLimiter.tryAcquireViewKey(userId, hearitId);

        // then
        assertThat(first).isTrue();
        assertThat(second).isFalse();
    }

    @Test
    @DisplayName("조회수에 대해 다른 사용자의 요청은 허용된다.")
    void differentUser_allowed() {
        // given
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();
        Long hearitId = 1L;

        boolean user1Try = rateLimiter.tryAcquireViewKey(user1, hearitId);
        boolean user2Try = rateLimiter.tryAcquireViewKey(user2, hearitId);

        assertThat(user1Try).isTrue();
        assertThat(user2Try).isTrue();
    }
}
