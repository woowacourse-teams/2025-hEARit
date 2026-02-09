package com.onair.hearit.app.hearit.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
public class ViewCountRateLimitScriptConfigTest {

    static GenericContainer<?> redisContainer;
    static RedisTemplate<String, String> redisTemplate;

    @Autowired
    DefaultRedisScript<Long> viewCountLimitScript;

    @BeforeAll
    static void startRedis() {
        redisContainer = new GenericContainer<>("redis:7.2-alpine").withExposedPorts(6379);
        redisContainer.start();

        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(
                redisContainer.getHost(),
                redisContainer.getFirstMappedPort()
        );

        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(config);
        connectionFactory.afterPropertiesSet();

        redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);

        StringRedisSerializer serializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(serializer);
        redisTemplate.setValueSerializer(serializer);

        redisTemplate.afterPropertiesSet();
    }

    @BeforeEach
    void clearRedis() {
        /*
            Redis TTL 기반 동작에 대한 테스트 독립성 확보를 위해 각 테스트마다 초기화
         */
        try (var connection = redisTemplate.getConnectionFactory().getConnection()) {
            connection.serverCommands().flushAll();
        }
    }

    @AfterAll
    static void stopRedis() {
        if (redisContainer != null) {
            redisContainer.stop();
        }
    }

    @Test
    @DisplayName("중복 키에 대해서 처음 요청한다면 1(true)를 반환한다.")
    void firstRequestTest() {
        // given
        String duplicatedKey = "view:user1:hearit1";
        String ttl = "1"; // TTL 1초

        // when
        Long result = redisTemplate.execute(
                viewCountLimitScript,
                List.of(duplicatedKey),
                ttl
        );

        // then
        assertThat(result).isEqualTo(1L);
    }

    @Test
    @DisplayName("중복 키에 대해서 TTL 이내에 다시 요청하면 0(false)를 반환한다.")
    void duplicatedRequestTest_beforeTTL() {
        // given
        String duplicatedKey = "view:user1:hearit1";
        String ttl = "1"; // TTL 1초

        // when
        redisTemplate.execute(
                viewCountLimitScript,
                List.of(duplicatedKey),
                ttl
        );
        Long second = redisTemplate.execute(
                viewCountLimitScript,
                List.of(duplicatedKey),
                ttl
        );

        // then
        assertThat(second).isEqualTo(0L);
    }

    @Test
    @Disabled("Redis TTL 기반 테스트는 환경에서 대기가 발생해 비활성화")
    @DisplayName("중복 키에 대해서 TTL 이후에 다시 요청하면 1(true)를 반환한다.")
    void duplicatedRequestTest_afterTTL() throws InterruptedException {
        // given
        String duplicatedKey = "view:user1:hearit1";
        String ttl = "1"; // TTL 1초

        // when
        redisTemplate.execute(
                viewCountLimitScript,
                List.of(duplicatedKey),
                ttl
        );

        Thread.sleep(Duration.ofSeconds(2).toMillis());

        Long result = redisTemplate.execute(
                viewCountLimitScript,
                List.of(duplicatedKey),
                ttl
        );

        // then
        assertThat(result).isEqualTo(1L);
    }
}
