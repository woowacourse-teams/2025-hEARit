package com.onair.hearit.app.playinghistory.infrastructure.buffer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onair.hearit.app.exception.custom.RedisBufferException;
import com.onair.hearit.app.fixture.DbHelper;
import com.onair.hearit.app.playinghistory.infrastructure.buffer.config.CircuitBreakerConfig;
import com.onair.hearit.app.playinghistory.infrastructure.converter.PlayingHistoryConverter;
import com.onair.hearit.core.config.DataSourceConfig;
import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.Member;
import com.onair.hearit.core.domain.PlayingHistory;
import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import com.onair.hearit.core.infrastructure.jdbc.PlayingHistoryCommandRepository;
import com.onair.hearit.core.infrastructure.jpa.HearitRepository;
import com.onair.hearit.core.infrastructure.jpa.PlayingHistoryRepository;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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
import org.testcontainers.utility.DockerImageName;

@DataJpaTest
@Sql("/dbclean.sql")
@ActiveProfiles("integration-test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
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
class PlayingHistoryBufferFacadeTest {

    static GenericContainer<?> redisContainer;
    static LettuceConnectionFactory connectionFactory;
    static RedisTemplate<String, String> redisTemplate;
    static RedissonClient redissonClient;

    @Autowired
    DbHelper dbHelper;

    @Autowired
    HearitRepository hearitRepository;

    @Autowired
    PlayingHistoryRepository playingHistoryRepository;

    @Autowired
    PlayingHistoryCommandRepository commandRepository;

    @Autowired
    PlayingHistoryConverter converter;

    @Autowired
    CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired
    PlayingHistoryMapBuffer mapBuffer;

    PlayingHistoryRedisBuffer redisBuffer;
    PlayingHistoryRedisBufferWithCircuitBreaker circuitWrappedRedisBuffer;
    PlayingHistoryBufferFacade facade;
    CircuitBreaker circuitBreaker;

    @BeforeAll
    static void startRedis() {
        redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7.2-alpine"))
                .withExposedPorts(6379);
        redisContainer.start();

        // RedisTemplate 설정
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisContainer.getHost());
        config.setPort(redisContainer.getFirstMappedPort());

        connectionFactory = new LettuceConnectionFactory(config);
        connectionFactory.afterPropertiesSet();

        redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());
        redisTemplate.afterPropertiesSet();

        // Redisson 설정
        Config redissonConfig = new Config();
        redissonConfig.useSingleServer()
                .setAddress("redis://" + redisContainer.getHost() + ":" + redisContainer.getFirstMappedPort());
        redissonClient = Redisson.create(redissonConfig);
    }

    @AfterAll
    static void stopRedis() {
        if (redissonClient != null) {
            redissonClient.shutdown();
        }
        if (redisTemplate != null) {
            redisTemplate.getConnectionFactory().getConnection().close();
        }
        if (connectionFactory != null) {
            connectionFactory.destroy();
        }
        if (redisContainer != null) {
            redisContainer.stop();
        }
    }

    @BeforeEach
    void setup() {
        // Redis 초기화
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();

        mapBuffer.clear();

        circuitBreaker = circuitBreakerRegistry.circuitBreaker("redisBuffer");
        circuitBreaker.reset(); // CLOSED 상태로 초기화

        redisBuffer = new PlayingHistoryRedisBuffer(
                redisTemplate,
                redissonClient,
                commandRepository,
                converter,
                new ObjectMapper(),
                1000L,  // lockWaitTime
                3000L   // lockLeaseTime
        );
        circuitWrappedRedisBuffer = new PlayingHistoryRedisBufferWithCircuitBreaker(redisBuffer, circuitBreaker);
        facade = new PlayingHistoryBufferFacade(circuitWrappedRedisBuffer, mapBuffer);
    }

    @AfterEach
    void ensureRedisRunning() throws InterruptedException {
        if (!redisContainer.isRunning()) {
            redisContainer.start();
            waitForRedis();
        }
    }

    private void waitForRedis() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            try {
                redisTemplate.getConnectionFactory().getConnection().ping();
                break;
            } catch (Exception e) {
                Thread.sleep(300);
            }
        }
    }

    @Nested
    @DisplayName("정상 상태 동작 (CLOSED)")
    class NormalOperation {

        @Test
        @DisplayName("CLOSED 상태에서는 정상적으로 Redis에 저장된다")
        void closed_usesRedis() {
            // given
            Member member = dbHelper.insertMember(TestFixture.createFixedMember());
            Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
            Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
            PlayingHistory history = new PlayingHistory(member.getUuid(), hearit, 5000L);
            assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);

            // when
            facade.add(history, 1000L);

            // then
            assertAll(
                    () -> assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED),
                    () -> assertThat(redisBuffer.size()).isEqualTo(1),
                    () -> assertThat(mapBuffer.size()).isEqualTo(0)
            );
        }

        @Test
        @DisplayName("CLOSED 상태에서 여러 요청이 모두 Redis에 저장된다")
        void closed_multipleRequests_allUseRedis() {
            // given
            assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);

            Member member = dbHelper.insertMember(TestFixture.createFixedMember());
            Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());

            int requestCount = 10;

            // when
            for (int i = 0; i < requestCount; i++) {
                Hearit hearit = dbHelper.insertHearit(
                        TestFixture.createFixedHearitWith(category)
                );
                facade.add(
                        new PlayingHistory(member.getUuid(), hearit, 1000L + i),
                        1000L + i
                );
            }

            // then
            assertAll(
                    () -> assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED),
                    () -> assertThat(redisBuffer.size()).isEqualTo(requestCount),
                    () -> assertThat(mapBuffer.size()).isEqualTo(0)
            );
        }
    }

    @Nested
    @DisplayName("Circuit OPEN 상태에서 fallback 동작")
    class CircuitOpenFallback {

        @Test
        @DisplayName("Circuit OPEN 시 Redis를 호출하지 않고 즉시 Map Buffer로 저장한다")
        void circuitOpen_immediatelyUsesMapFallback() {
            // given
            Member member = dbHelper.insertMember(TestFixture.createFixedMember());
            Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
            Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
            PlayingHistory history = new PlayingHistory(member.getUuid(), hearit, 5000L);

            // Circuit을 강제로 OPEN
            circuitBreaker.transitionToOpenState();

            // when
            facade.add(history, 1000L);

            // then
            assertAll(
                    () -> assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN),
                    () -> assertThat(mapBuffer.size()).isEqualTo(1),
                    () -> assertThat(redisBuffer.size()).isEqualTo(0)
            );
        }

        @Test
        @DisplayName("Circuit OPEN 상태에서 여러 요청이 모두 Map Buffer에 저장된다")
        void circuitOpen_multipleRequests_allUseMapBuffer() {
            // given
            circuitBreaker.transitionToOpenState();
            Member member = dbHelper.insertMember(TestFixture.createFixedMember());
            Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());

            int requestCount = 10;

            // when
            for (int i = 0; i < requestCount; i++) {
                Hearit hearit = dbHelper.insertHearit(
                        TestFixture.createFixedHearitWith(category)
                );
                facade.add(
                        new PlayingHistory(member.getUuid(), hearit, 1000L + i),
                        1000L + i
                );
            }

            // then
            assertAll(
                    () -> assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN),
                    () -> assertThat(mapBuffer.size()).isEqualTo(requestCount),
                    () -> assertThat(redisBuffer.size()).isEqualTo(0)
            );
        }
    }

    @Nested
    @DisplayName("Redis 인프라 장애 시 fallback 동작")
    class RedisInfraFailureFallback {

        @Test
        @DisplayName("RedisBufferException 발생 시 Map Buffer로 fallback한다")
        void redisBufferException_usesFallback() {
            // given
            Member member = dbHelper.insertMember(TestFixture.createFixedMember());
            Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
            Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
            PlayingHistory history = new PlayingHistory(member.getUuid(), hearit, 5000L);

            // 임의로 RedisBufferException 발생시킴
            PlayingHistoryRedisBuffer spyRedisBuffer = spy(redisBuffer);
            doThrow(new RedisBufferException("Redis 연결 실패"))
                    .when(spyRedisBuffer).add(any(), anyLong());

            PlayingHistoryRedisBufferWithCircuitBreaker spyCircuitWrappedBuffer =
                    new PlayingHistoryRedisBufferWithCircuitBreaker(spyRedisBuffer, circuitBreaker);

            PlayingHistoryBufferFacade spyFacade = new PlayingHistoryBufferFacade(
                    spyCircuitWrappedBuffer,
                    mapBuffer
            );

            // when
            spyFacade.add(history, 1000L);

            // then
            assertAll(
                    () -> assertThat(redisBuffer.size()).isEqualTo(0),
                    () -> assertThat(mapBuffer.size()).isEqualTo(1),
                    () -> assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED)
            );
        }
    }
}
