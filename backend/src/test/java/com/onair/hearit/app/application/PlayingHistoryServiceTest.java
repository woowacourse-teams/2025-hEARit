package com.onair.hearit.app.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.app.dto.request.PlayingHistoryRequest;
import com.onair.hearit.app.infrastructure.scheduler.PlayingHistoryBuffer;
import com.onair.hearit.auth.domain.UserContext;
import com.onair.hearit.common.domain.Category;
import com.onair.hearit.common.domain.Hearit;
import com.onair.hearit.common.domain.Member;
import com.onair.hearit.common.domain.PlayingHistory;
import com.onair.hearit.common.domain.Source;
import com.onair.hearit.common.exception.custom.NotFoundException;
import com.onair.hearit.common.exception.custom.UnauthorizedException;
import com.onair.hearit.common.infrastructure.jdbc.PlayingHistoryCommandRepository;
import com.onair.hearit.common.infrastructure.jpa.HearitRepository;
import com.onair.hearit.common.infrastructure.jpa.PlayingHistoryRepository;
import com.onair.hearit.common.infrastructure.jpa.TestJpaAuditingConfig;
import com.onair.hearit.fixture.DbHelper;
import com.onair.hearit.fixture.TestFixture;
import java.util.List;
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
@Import({DbHelper.class, TestJpaAuditingConfig.class, PlayingHistoryBuffer.class,
        PlayingHistoryCommandRepository.class})
class PlayingHistoryServiceTest {

    @Autowired
    private DbHelper dbHelper;

    @Autowired
    private PlayingHistoryRepository playingHistoryRepository;

    @Autowired
    private PlayingHistoryBuffer playingHistoryBuffer;

    @Autowired
    private HearitRepository hearitRepository;

    private PlayingHistoryService playingHistoryService;

    @BeforeEach
    void setup() {
        playingHistoryService = new PlayingHistoryService(playingHistoryRepository, playingHistoryBuffer,
                hearitRepository);
    }

    @Test
    @DisplayName("로그인한 회원은 재생기록을 저장할 수 있다.")
    void addPlayHistory() throws InterruptedException {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        Category category = dbHelper.insertCategory(new Category("name", "#000000"));
        Hearit hearit = dbHelper.insertHearit(createHearitWith(100, category));
        PlayingHistoryRequest request = new PlayingHistoryRequest(hearit.getId(), 100L);

        // when
        playingHistoryService.addPlayingHistory(UserContext.member(member.getId()), request);
        playingHistoryBuffer.flush();

        // then
        List<PlayingHistory> playingHistories = playingHistoryRepository.findAll();
        assertAll(() -> {
            assertThat(playingHistories.size()).isEqualTo(1);
            assertThat(playingHistories.getFirst().getMemberId()).isEqualTo(member.getId());
            assertThat(playingHistories.getFirst().getHearitId()).isEqualTo(hearit.getId());
        });
    }

    @Test
    @DisplayName("로그인한 회원은 재생기록을 수정할 수 있다.")
    void modifyPlayHistory() throws InterruptedException {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        Category category = dbHelper.insertCategory(new Category("name", "#000000"));
        Hearit hearit = dbHelper.insertHearit(createHearitWith(100, category));
        PlayingHistory playingHistory = dbHelper.insertPlayingHistory(
                new PlayingHistory(member.getId(), hearit, 10_000));
        PlayingHistoryRequest request = new PlayingHistoryRequest(hearit.getId(), 50_000L);

        // when
        playingHistoryService.addPlayingHistory(UserContext.member(member.getId()), request);
        playingHistoryBuffer.flush();

        // then
        List<PlayingHistory> playingHistories = playingHistoryRepository.findAll();
        assertAll(() -> {
            assertThat(playingHistories.size()).isEqualTo(1);
            assertThat(playingHistories.getFirst().getMemberId()).isEqualTo(member.getId());
            assertThat(playingHistories.getFirst().getHearitId()).isEqualTo(hearit.getId());
        });
    }

    @Test
    @DisplayName("로그인하지 않은 회원은 재생기록을 저장할 수 없다.")
    void checkMember() {
        // given
        Category category = dbHelper.insertCategory(new Category("name", "#000000"));
        Hearit hearit = dbHelper.insertHearit(createHearitWith(100, category));
        PlayingHistoryRequest request = new PlayingHistoryRequest(hearit.getId(), 100L);

        // when & then
        assertThatThrownBy(() -> playingHistoryService.addPlayingHistory(UserContext.guest(), request))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("존재하지 않는 히어릿에 대한 재생기록을 저장할 수 없다.")
    void checkHearit() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        PlayingHistoryRequest request = new PlayingHistoryRequest(1L, 100L);

        // when
        assertThatThrownBy(() -> playingHistoryService.addPlayingHistory(UserContext.member(member.getId()), request))
                .isInstanceOf(NotFoundException.class);
    }

    private Hearit createHearitWith(int playTime, Category category) {
        return new Hearit("title",
                "summary",
                playTime,
                "/hearit/audio/original/ORG_bf7c513e-579e-4224-8505-3824bb22ed01.mp3",
                "/hearit/audio/short/SHR_bf7c513e-579e-4224-8505-3824bb22ed01.mp3",
                "/hearit/script/SCR_bf7c513e-579e-4224-8505-3824bb22ed01.json",
                List.of(new Source("원본은 CC BY 4.0 라이선스를 따릅니다.", "https://example.com/2")),
                category
        );
    }
}
