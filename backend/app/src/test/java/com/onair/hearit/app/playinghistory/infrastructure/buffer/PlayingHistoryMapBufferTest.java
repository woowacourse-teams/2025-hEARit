package com.onair.hearit.app.playinghistory.infrastructure.buffer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

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
@Import({DbHelper.class, TestJpaAuditingConfig.class, DataSourceConfig.class, PlayingHistoryCommandRepository.class})
class PlayingHistoryMapBufferTest {

    @Autowired
    DbHelper dbHelper;

    @Autowired
    PlayingHistoryCommandRepository commandRepository;

    @Autowired
    HearitRepository hearitRepository;

    @Autowired
    PlayingHistoryRepository playingHistoryRepository;

    PlayingHistoryMapBuffer buffer;

    @BeforeEach
    void setup() {
        buffer = new PlayingHistoryMapBuffer(commandRepository, hearitRepository);
    }

    @Test
    @DisplayName("버퍼에 데이터 추가 후 flush 시 데이터가 저장된다")
    void testAddAndFlush() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        PlayingHistory history = new PlayingHistory(member.getUuid(), hearit, 50L);

        // when
        buffer.add(history, 1_000L);
        buffer.flush();

        // then
        assertThat(playingHistoryRepository.findAll().size()).isEqualTo(1);
    }

    @Test
    @DisplayName("과거 데이터라도 clientEventTime이 최신이면 덮어써진다")
    void testRecentClientEventTimeWins() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        // lastPlayTime 100이지만 clientEventTime 1_000 (과거 이벤트)
        PlayingHistory oldHistory = new PlayingHistory(member.getUuid(), hearit, 100L);
        // lastPlayTime 50이지만 clientEventTime 2_000 (최근 이벤트)
        PlayingHistory newHistory = new PlayingHistory(member.getUuid(), hearit, 50L);

        buffer.add(newHistory, 2_000L);  // 최근 데이터 추가
        buffer.add(oldHistory, 1_000L);  // 네트워크 지연으로 과거 데이터 도착

        // when
        buffer.flush();

        // then
        PlayingHistory saved = playingHistoryRepository.findAll().get(0);
        assertThat(saved.getLastPlayTime()).isEqualTo(50L); // clientEventTime 기준 최신이 적용
    }

    @Test
    @DisplayName("flush 중 예외 발생 시 rollback으로 Map에 값이 남아 있어야 하고 DB에는 저장되지 않는다.")
    void testFlushRollbackMapAndDb() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Hearit hearit2 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        PlayingHistory history1 = new PlayingHistory(member.getUuid(), hearit1, 50L);
        PlayingHistory history2 = new PlayingHistory(member.getUuid(), hearit2, 60L);

        buffer.add(history1, 1_000L);
        buffer.add(history2, 2_000L);

        // spy repository로 bulkInsert에서 예외 발생
        PlayingHistoryCommandRepository spyRepo = spy(commandRepository);
        doThrow(new RuntimeException("DB error")).when(spyRepo).bulkInsert(anyList());
        PlayingHistoryMapBuffer failingBuffer = new PlayingHistoryMapBuffer(spyRepo, hearitRepository);

        failingBuffer.add(history1, 1_000L);
        failingBuffer.add(history2, 2_000L);

        // when
        failingBuffer.flush();

        // then
        assertAll(
                () -> assertThat(playingHistoryRepository.findAll()).isEmpty(),
                () -> assertThat(failingBuffer.getCache().size()).isEqualTo(2)
        );
    }
}
