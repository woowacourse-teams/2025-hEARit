package com.onair.hearit.app.playinghistory.infrastructure.buffer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onair.hearit.app.fixture.RedisIntegrationTestSupport;
import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.PlayingHistory;
import com.onair.hearit.core.fixture.TestFixture;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PlayingHistoryRedisBufferTest extends RedisIntegrationTestSupport {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private PlayingHistoryRedisBuffer storage;

    @BeforeEach
    void setup() {
        storage = new PlayingHistoryRedisBuffer(
                redisTemplate,
                redissonClient,
                commandRepository,
                converter,
                objectMapper,
                1000L,
                3000L
        );
    }

    @Test
    @DisplayName("Redis에 재생 기록을 저장한다")
    void add_success() {
        // given
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        PlayingHistory history = new PlayingHistory(UUID.randomUUID(), hearit, 5000L);

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

        PlayingHistory oldHistory = new PlayingHistory(UUID.fromString("00000000-0000-0000-0000-000000000001"), hearit, 100L);
        PlayingHistory newHistory = new PlayingHistory(UUID.fromString("00000000-0000-0000-0000-000000000001"), hearit, 50L);

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

        storage.add(new PlayingHistory(UUID.fromString("00000000-0000-0000-0000-000000000001"), hearit1, 5000L), 1000L);
        storage.add(new PlayingHistory(UUID.fromString("00000000-0000-0000-0000-000000000002"), hearit2, 10000L), 2000L);

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
        storage.add(new PlayingHistory(UUID.fromString("00000000-0000-0000-0000-000000000001"), hearit1, 5000L), 1000L);
        storage.add(new PlayingHistory(UUID.fromString("00000000-0000-0000-0000-000000000002"), hearit2, 10000L), 2000L);

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
        storage.add(new PlayingHistory(UUID.fromString("00000000-0000-0000-0000-000000000001"), hearit, 5000L), 1000L);
        storage.add(new PlayingHistory(UUID.fromString("00000000-0000-0000-0000-000000000001"), hearit, 10000L), 2000L);

        // then
        assertThat(storage.size()).isEqualTo(1); // 하나만 존재 (덮어씀)
    }
}
