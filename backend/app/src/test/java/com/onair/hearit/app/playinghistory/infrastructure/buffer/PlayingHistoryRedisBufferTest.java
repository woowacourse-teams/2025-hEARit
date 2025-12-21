package com.onair.hearit.app.playinghistory.infrastructure.buffer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onair.hearit.app.fixture.DbHelper;
import com.onair.hearit.app.playinghistory.infrastructure.converter.PlayingHistoryConverter;
import com.onair.hearit.core.config.DataSourceConfig;
import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.PlayingHistory;
import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import com.onair.hearit.core.infrastructure.jdbc.PlayingHistoryCommandRepository;
import com.onair.hearit.core.infrastructure.jpa.HearitRepository;
import com.onair.hearit.core.infrastructure.jpa.PlayingHistoryRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
        PlayingHistoryConverter.class
})
class PlayingHistoryRedisBufferTest {

    static GenericContainer<?> redisContainer;
    static RedisTemplate<String, String> redisTemplate;
    static RedissonClient redissonClient;

    @Autowired
    DbHelper dbHelper;

    @Autowired
    HearitRepository hearitRepository;

    @Autowired
    PlayingHistoryCommandRepository commandRepository;

    @Autowired
    PlayingHistoryRepository playingHistoryRepository;

    @Autowired
    PlayingHistoryConverter converter;

    ObjectMapper objectMapper = new ObjectMapper();
    PlayingHistoryRedisBuffer storage;

    @BeforeAll
    static void startRedis() {
        redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7.2-alpine"))
                .withExposedPorts(6379);
        redisContainer.start();

        // RedisTemplate 설정
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisContainer.getHost());
        config.setPort(redisContainer.getFirstMappedPort());

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
        if (redisContainer != null) {
            redisContainer.stop();
        }
    }

    @BeforeEach
    void setup() {
        // Redis 데이터 초기화
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();

        storage = new PlayingHistoryRedisBuffer(
                redisTemplate,
                redissonClient,
                commandRepository,
                converter,
                objectMapper
        );
    }

    @Test
    @DisplayName("Redis에 재생 기록을 저장한다")
    void add_success() {
        // given
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        PlayingHistory history = new PlayingHistory("user-uuid", hearit, 5000L);

        // when
        storage.add(history, 1000L);

        // then
        assertThat(storage.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("기존 데이터가 더 최신이면 업데이트하지 않는다")
    void add_existingIsMoreRecent() {
        // given
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        PlayingHistory oldHistory = new PlayingHistory("user-uuid", hearit, 100L);
        PlayingHistory newHistory = new PlayingHistory("user-uuid", hearit, 50L);

        // when
        storage.add(newHistory, 2000L); // clientEventTime이 더 최신
        storage.add(oldHistory, 1000L); // clientEventTime이 과거

        // then
        assertThat(storage.size()).isEqualTo(1); // 하나만 존재
    }

    @Test
    @DisplayName("Redis 데이터를 DB로 flush한다")
    void flush_success() {
        // given
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Hearit hearit2 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        storage.add(new PlayingHistory("user1", hearit1, 5000L), 1000L);
        storage.add(new PlayingHistory("user2", hearit2, 10000L), 2000L);

        // when
        storage.flush();

        // then
        assertAll(
                () -> assertThat(storage.size()).isEqualTo(0),
                () -> assertThat(playingHistoryRepository.findAll()).hasSize(2)
        );
    }

    @Test
    @DisplayName("Redis가 비어있으면 flush하지 않는다")
    void flush_emptyRedis() {
        // when & then - no exception
        storage.flush();
        assertThat(storage.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("size()로 Redis 크기를 조회한다")
    void size() {
        // given
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Hearit hearit2 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        // when
        storage.add(new PlayingHistory("user1", hearit1, 5000L), 1000L);
        storage.add(new PlayingHistory("user2", hearit2, 10000L), 2000L);

        // then
        assertThat(storage.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("동일 사용자의 동일 Hearit에 대한 중복 추가는 최신 데이터로 덮어쓴다")
    void add_duplicate_overwrites() {
        // given
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        // when
        storage.add(new PlayingHistory("user-uuid", hearit, 5000L), 1000L);
        storage.add(new PlayingHistory("user-uuid", hearit, 10000L), 2000L);

        // then
        assertThat(storage.size()).isEqualTo(1); // 하나만 존재 (덮어씀)
    }
}
