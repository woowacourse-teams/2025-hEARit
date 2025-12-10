package com.onair.hearit.app.playinghistory.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.app.auth.domain.RequestUser;
import com.onair.hearit.core.config.DataSourceConfig;
import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.Member;
import com.onair.hearit.core.domain.PlayingHistory;
import com.onair.hearit.core.domain.Source;
import com.onair.hearit.core.domain.UserInfo;
import com.onair.hearit.app.exception.custom.NotFoundException;
import com.onair.hearit.app.fixture.DbHelper;
import com.onair.hearit.core.infrastructure.jdbc.PlayingHistoryCommandRepository;
import com.onair.hearit.core.infrastructure.jpa.HearitRepository;
import com.onair.hearit.core.infrastructure.jpa.PlayingHistoryRepository;
import com.onair.hearit.app.playinghistory.dto.PlayingHistoryRequest;
import com.onair.hearit.app.playinghistory.dto.RecentlyPlayedHearitResponse;
import com.onair.hearit.app.playinghistory.infrastructure.buffer.PlayingHistoryQueueBuffer;
import java.util.List;
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
@Import({DbHelper.class, TestJpaAuditingConfig.class, DataSourceConfig.class,
        PlayingHistoryQueueBuffer.class, PlayingHistoryCommandRepository.class})
class PlayingHistoryServiceTest {

    @Autowired
    private DbHelper dbHelper;

    @Autowired
    private PlayingHistoryRepository playingHistoryRepository;

    @Autowired
    private PlayingHistoryQueueBuffer playingHistoryQueueBuffer;

    @Autowired
    private HearitRepository hearitRepository;

    private PlayingHistoryService playingHistoryService;

    @BeforeEach
    void setup() {
        playingHistoryService = new PlayingHistoryService(
                hearitRepository,
                playingHistoryRepository,
                playingHistoryQueueBuffer);
    }

    @Test
    @DisplayName("회원은 최근 재생 기록을 조회할 수 있다.")
    void getRecentPlayingHistoryOfMember_whenMember() throws InterruptedException {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        UserInfo memberInfo = RequestUser.member(member.getId()).getUserInfo();
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Hearit hearit2 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        dbHelper.insertPlayingHistory(new PlayingHistory(memberInfo.getMemberId(), hearit1, 10));
        Thread.sleep(1000);
        dbHelper.insertPlayingHistory(new PlayingHistory(memberInfo.getMemberId(), hearit2, 20));

        // when
        List<RecentlyPlayedHearitResponse> result = playingHistoryService.getRecentPlayingHistory(memberInfo);

        // then
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result.get(0).id()).isEqualTo(hearit2.getId()),
                () -> assertThat(result.get(1).id()).isEqualTo(hearit1.getId())
        );
    }

    @Test
    @DisplayName("회원이 아닌 유저는 빈 재생 기록을 반환한다.")
    void getRecentPlayingHistoryOfMember_whenGuestOrNull_thenReturnEmpty() {
        // given
        UserInfo guestInfo = RequestUser.guest(UUID.randomUUID().toString()).getUserInfo();

        // when
        List<RecentlyPlayedHearitResponse> guestResult = playingHistoryService.getRecentPlayingHistory(guestInfo);

        // then
        assertThat(guestResult).isEmpty();
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
        playingHistoryService.addPlayingHistory(TestFixture.createFixedMemberUserInfo(member), request);
        playingHistoryQueueBuffer.flush();

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
        playingHistoryService.addPlayingHistory(TestFixture.createFixedMemberUserInfo(member), request);
        playingHistoryQueueBuffer.flush();

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

        // when
        playingHistoryService.addPlayingHistory(TestFixture.createFixedGuestUserInfo(UUID.randomUUID().toString()),
                request);

        // then
        assertThat(playingHistoryRepository.findAll()).hasSize(0);
    }

    @Test
    @DisplayName("존재하지 않는 히어릿에 대한 재생기록을 저장할 수 없다.")
    void checkHearit() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        PlayingHistoryRequest request = new PlayingHistoryRequest(1L, 100L);

        // when
        assertThatThrownBy(
                () -> playingHistoryService.addPlayingHistory(TestFixture.createFixedMemberUserInfo(member), request))
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
