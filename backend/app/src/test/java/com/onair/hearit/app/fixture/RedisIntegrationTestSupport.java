package com.onair.hearit.app.fixture;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onair.hearit.app.playinghistory.infrastructure.buffer.PlayingHistoryMapBuffer;
import com.onair.hearit.app.playinghistory.infrastructure.buffer.TestCircuitBreakerConfig;
import com.onair.hearit.app.playinghistory.infrastructure.buffer.config.CircuitBreakerConfig;
import com.onair.hearit.app.playinghistory.infrastructure.converter.PlayingHistoryConverter;
import com.onair.hearit.core.config.DataSourceConfig;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import com.onair.hearit.core.infrastructure.jdbc.PlayingHistoryCommandRepository;
import com.onair.hearit.core.infrastructure.jpa.HearitRepository;
import com.onair.hearit.core.infrastructure.jpa.PlayingHistoryRepository;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Redis + DB 통합 테스트를 위한 DataJpaTest 기반 공통 클래스
 *
 * <p>주요 특징:
 * <ul>
 *   <li>@DataJpaTest: 필요한 Bean만 로딩, 빠른 초기화</li>
 *   <li>Singleton Redis 컨테이너: @Container로 JUnit이 lifecycle 관리</li>
 *   <li>Static Redis 객체: @BeforeAll에서 초기화, @AfterAll에서 정리</li>
 *   <li>@Sql 기반 DB 초기화: 각 테스트 메서드마다 dbclean.sql 실행</li>
 *   <li>컨텍스트 재사용: 모든 테스트가 동일한 @Import로 단일 컨텍스트 공유</li>
 *   <li>안전한 Redis flush: @BeforeEach에서 execute() 방식 사용</li>
 * </ul>
 */
@DataJpaTest
@Sql("/dbclean.sql")
@ActiveProfiles("integration-test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Testcontainers
@Import({
        DbHelper.class,
        TestJpaAuditingConfig.class,
        DataSourceConfig.class,
        PlayingHistoryCommandRepository.class,
        PlayingHistoryConverter.class,
        PlayingHistoryMapBuffer.class,
        TestCircuitBreakerConfig.class,
        CircuitBreakerConfig.class
})
public abstract class RedisIntegrationTestSupport {

    @Container
    protected static final GenericContainer<?> REDIS_CONTAINER =
            new GenericContainer<>(DockerImageName.parse("redis:7.2-alpine"))
                    .withExposedPorts(6379);

    protected static RedisTemplate<String, String> redisTemplate;
    protected static RedissonClient redissonClient;

    /**
     * Redis 객체 초기화
     * - @Container가 컨테이너를 시작한 후 실행됨 (JUnit이 보장)
     * - static 초기화로 모든 테스트가 동일한 인스턴스 공유
     */
    @BeforeAll
    static void initializeRedis() {
        // RedisTemplate 설정
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(REDIS_CONTAINER.getHost());
        config.setPort(REDIS_CONTAINER.getFirstMappedPort());

        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(config);
        connectionFactory.afterPropertiesSet();

        redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        StringRedisSerializer serializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(serializer);
        redisTemplate.setValueSerializer(serializer);
        redisTemplate.setHashKeySerializer(serializer);
        redisTemplate.setHashValueSerializer(serializer);
        redisTemplate.afterPropertiesSet();

        // Redisson 설정 (테스트 환경 최적화: pool 10→2, idle 1→0)
        Config redissonConfig = new Config();
        redissonConfig.useSingleServer()
                .setAddress("redis://" + REDIS_CONTAINER.getHost() + ":" + REDIS_CONTAINER.getFirstMappedPort())
                .setConnectionPoolSize(2)           // 테스트용 축소
                .setConnectionMinimumIdleSize(0)    // 테스트용 축소
                .setConnectTimeout(3000)
                .setTimeout(500)
                .setRetryAttempts(1)
                .setRetryInterval(100);
        redissonClient = Redisson.create(redissonConfig);
    }

    /**
     * Redis 리소스 정리
     * - Redisson shutdown으로 커넥션 정리
     */
    @AfterAll
    static void cleanupRedis() {
        if (redissonClient != null && !redissonClient.isShutdown()) {
            redissonClient.shutdown();
        }
    }

    // ========================================
    // 공통 Autowired Bean들
    // ========================================

    @Autowired
    protected DbHelper dbHelper;

    @Autowired
    protected HearitRepository hearitRepository;

    @Autowired
    protected PlayingHistoryRepository playingHistoryRepository;

    @Autowired
    protected PlayingHistoryCommandRepository commandRepository;

    @Autowired
    protected PlayingHistoryConverter converter;

    @Autowired
    protected PlayingHistoryMapBuffer mapBuffer;

    @Autowired
    protected CircuitBreakerRegistry circuitBreakerRegistry;

    /** ObjectMapper (RedisBuffer 생성용) */
    protected final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 각 테스트 실행 전 Redis 데이터 초기화
     * - DB는 @Sql이 자동 정리
     * - Redis는 수동으로 flushAll 필요
     * - execute() 방식으로 connection을 안전하게 관리
     */
    @BeforeEach
    protected void clearRedis() {
        redisTemplate.execute((org.springframework.data.redis.core.RedisCallback<Void>) connection -> {
            connection.serverCommands().flushAll();
            return null;
        });
    }
}
