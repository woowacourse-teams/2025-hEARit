package com.onair.hearit.app.playinghistory.infrastructure.buffer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.app.fixture.DbHelper;
import com.onair.hearit.app.playinghistory.infrastructure.buffer.converter.PlayingHistoryConverter;
import com.onair.hearit.core.config.DataSourceConfig;
import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.Hearit;
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
@Import({
        DbHelper.class,
        TestJpaAuditingConfig.class,
        DataSourceConfig.class,
        PlayingHistoryCommandRepository.class,
        PlayingHistoryConverter.class
})
class PlayingHistoryBufferFacadeTest {

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

    PlayingHistoryMapBuffer mapBuffer;

    @BeforeEach
    void setup() {
        mapBuffer = new PlayingHistoryMapBuffer(commandRepository, converter);
    }

    @Test
    @DisplayName("PlayingHistoryMapBuffer에 데이터를 추가하고 flush하면 DB에 저장된다")
    void add_and_flush() throws Exception {
        // given
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        PlayingHistory history = new PlayingHistory("user-uuid", hearit, 5000L);

        // when
        mapBuffer.add(history, 1000L);
        int sizeBefore = mapBuffer.size();
        mapBuffer.flush();
        int sizeAfter = mapBuffer.size();

        // then
        assertAll(
                () -> assertThat(sizeBefore).isEqualTo(1),
                () -> assertThat(sizeAfter).isEqualTo(0),
                () -> assertThat(playingHistoryRepository.findAll()).hasSize(1)
        );
    }

    @Test
    @DisplayName("동일 사용자의 동일 Hearit에 대한 중복 추가 시 최신 데이터로 덮어쓴다")
    void add_duplicate_overwrites() throws Exception {
        // given
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        PlayingHistory history1 = new PlayingHistory("user-uuid", hearit, 5000L);
        PlayingHistory history2 = new PlayingHistory("user-uuid", hearit, 10000L);

        // when
        mapBuffer.add(history1, 1000L);
        mapBuffer.add(history2, 2000L); // 더 최신

        // then
        assertThat(mapBuffer.size()).isEqualTo(1); // 하나만 존재
    }

    @Test
    @DisplayName("size()로 버퍼 크기를 확인할 수 있다")
    void size() throws Exception {
        // given
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Hearit hearit2 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        // when
        mapBuffer.add(new PlayingHistory("user1", hearit1, 5000L), 1000L);
        mapBuffer.add(new PlayingHistory("user2", hearit2, 6000L), 1000L);

        // then
        assertThat(mapBuffer.size()).isEqualTo(2);
    }

}
