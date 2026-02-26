package com.onair.hearit.app.playinghistory.infrastructure.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.app.fixture.DbHelper;
import com.onair.hearit.app.playinghistory.infrastructure.buffer.PlayingHistoryMapBuffer;
import com.onair.hearit.app.playinghistory.infrastructure.buffer.TestCircuitBreakerConfig;
import com.onair.hearit.app.playinghistory.infrastructure.buffer.config.CircuitBreakerConfig;
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
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

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
class PlayingHistoryFlushSchedulerTest {

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

    PlayingHistoryMapBuffer buffer;
    PlayingHistoryFlushScheduler scheduler;

    @BeforeEach
    void setup() {
        buffer = new PlayingHistoryMapBuffer(commandRepository, converter);
        scheduler = new PlayingHistoryFlushScheduler(buffer);
    }

    @Test
    @DisplayName("스케줄러가 버퍼를 flush한다")
    void scheduleFlush_success() {
        // given
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        buffer.add(new PlayingHistory(UUID.fromString("00000000-0000-0000-0000-000000000001"), hearit, 5000L), 1000L);

        // when
        scheduler.scheduleFlush();

        // then
        assertAll(
                () -> assertThat(buffer.size()).isEqualTo(0),
                () -> assertThat(playingHistoryRepository.findAll()).hasSize(1)
        );
    }

    @Test
    @DisplayName("애플리케이션 종료 시 버퍼를 flush한다")
    void shutdown() {
        // given
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        buffer.add(new PlayingHistory(UUID.fromString("00000000-0000-0000-0000-000000000001"), hearit, 5000L), 1000L);

        // when
        scheduler.shutdown();

        // then
        assertAll(
                () -> assertThat(buffer.size()).isEqualTo(0),
                () -> assertThat(playingHistoryRepository.findAll()).hasSize(1)
        );
    }
}
