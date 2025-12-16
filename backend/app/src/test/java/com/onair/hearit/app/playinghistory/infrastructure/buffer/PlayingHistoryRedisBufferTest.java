package com.onair.hearit.app.playinghistory.infrastructure.buffer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onair.hearit.app.fixture.DbHelper;
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
import org.junit.jupiter.api.AfterEach;
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

@DataJpaTest
@Sql("/dbclean.sql")
@ActiveProfiles("integration-test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import({DbHelper.class, TestJpaAuditingConfig.class, DataSourceConfig.class, PlayingHistoryCommandRepository.class})
class PlayingHistoryRedisBufferTest {

    @Autowired
    DbHelper dbHelper;

    @Autowired
    PlayingHistoryCommandRepository commandRepository;

    @Autowired
    HearitRepository hearitRepository;

    @Autowired
    PlayingHistoryRepository playingHistoryRepository;

    RedisTemplate<String, String> redisTemplate;
    RedissonClient redissonClient;
    PlayingHistoryRedisBuffer buffer;
    ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        // Redis 연결 설정 (테스트용)
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName("localhost");
        redisConfig.setPort(6379);
        redisConfig.setPassword("my_strong_redis_password_123!");

        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(redisConfig);
        connectionFactory.afterPropertiesSet();

        redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());
        redisTemplate.afterPropertiesSet();

        // Redisson 설정
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://localhost:6379")
                .setPassword("my_strong_redis_password_123!");
        redissonClient = Redisson.create(config);

        objectMapper = new ObjectMapper();

        buffer = new PlayingHistoryRedisBuffer(
                redisTemplate,
                redissonClient,
                commandRepository,
                hearitRepository,
                objectMapper
        );
    }

    @AfterEach
    void cleanup() {
        // Redis 데이터 정리
        redisTemplate.delete("playing_history");
        if (redissonClient != null) {
            redissonClient.shutdown();
        }
    }

    @Test
    @DisplayName("Redis 버퍼에 데이터 추가 후 flush 시 DB에 저장되고 Redis에서 삭제된다")
    void testAddAndFlush() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        PlayingHistory history = new PlayingHistory(member.getUuid(), hearit, 50L);

        // when
        buffer.add(history, 1_000L);

        // Redis에 데이터가 있는지 확인
        Long redisSize = redisTemplate.opsForHash().size("playing_history");
        assertThat(redisSize).isEqualTo(1);

        // flush 실행
        buffer.flush();

        // then
        assertAll(
                () -> assertThat(playingHistoryRepository.findAll().size()).isEqualTo(1),
                () -> assertThat(redisTemplate.opsForHash().size("playing_history")).isEqualTo(0)
        );
    }

    @Test
    @DisplayName("clientEventTime이 최신인 데이터만 Redis에 저장된다")
    void testRecentClientEventTimeWins() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        // 최근 데이터 먼저 추가
        PlayingHistory recentHistory = new PlayingHistory(member.getUuid(), hearit, 100L);
        buffer.add(recentHistory, 2_000L);

        // 과거 데이터 나중에 추가 (네트워크 지연 시뮬레이션)
        PlayingHistory oldHistory = new PlayingHistory(member.getUuid(), hearit, 50L);
        buffer.add(oldHistory, 1_000L);

        // when
        buffer.flush();

        // then
        PlayingHistory saved = playingHistoryRepository.findAll().get(0);
        assertThat(saved.getLastPlayTime()).isEqualTo(100L); // clientEventTime 2_000이 최신이므로 100L 저장
    }

    @Test
    @DisplayName("동일 사용자의 다른 Hearit 재생 기록은 독립적으로 저장된다")
    void testMultipleHearitsForSameUser() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Hearit hearit2 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        PlayingHistory history1 = new PlayingHistory(member.getUuid(), hearit1, 30L);
        PlayingHistory history2 = new PlayingHistory(member.getUuid(), hearit2, 60L);

        // when
        buffer.add(history1, 1_000L);
        buffer.add(history2, 2_000L);
        buffer.flush();

        // then
        assertAll(
                () -> assertThat(playingHistoryRepository.findAll().size()).isEqualTo(2),
                () -> assertThat(redisTemplate.opsForHash().size("playing_history")).isEqualTo(0)
        );
    }

    @Test
    @DisplayName("flush 실패 시 Redis에 데이터가 유지되고 DB에는 저장되지 않는다")
    void testFlushFailureKeepsDataInRedis() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        // 존재하지 않는 Hearit ID로 예외 발생 시뮬레이션
        PlayingHistory invalidHistory = new PlayingHistory(member.getUuid(), hearit, 50L);
        buffer.add(invalidHistory, 1_000L);

        // Hearit 삭제하여 flush 시 실패 유도
        hearitRepository.deleteById(hearit.getId());

        // when
        buffer.flush();

        // then (flush 실패 시 Redis 데이터는 유지되어야 함)
        assertAll(
                () -> assertThat(playingHistoryRepository.findAll()).isEmpty(),
                () -> assertThat(redisTemplate.opsForHash().size("playing_history")).isEqualTo(1) // Redis 데이터 유지
        );
    }
}
