package com.onair.hearit.app.playinghistory.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.app.auth.domain.RequestUser;
import com.onair.hearit.app.exception.custom.NotFoundException;
import com.onair.hearit.app.fixture.DbHelper;
import com.onair.hearit.app.playinghistory.dto.PlayingHistoryRequest;
import com.onair.hearit.app.playinghistory.dto.RecentlyPlayedHearitResponse;
import com.onair.hearit.app.playinghistory.infrastructure.buffer.PlayingHistoryBuffer;
import com.onair.hearit.app.playinghistory.infrastructure.buffer.PlayingHistoryMapBuffer;
import com.onair.hearit.app.userinfo.application.UserInfoService;
import com.onair.hearit.core.config.DataSourceConfig;
import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.Member;
import com.onair.hearit.core.domain.PlayingHistory;
import com.onair.hearit.core.domain.Source;
import com.onair.hearit.core.domain.UserInfo;
import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import com.onair.hearit.core.infrastructure.jdbc.PlayingHistoryCommandRepository;
import com.onair.hearit.core.infrastructure.jpa.HearitRepository;
import com.onair.hearit.core.infrastructure.jpa.PlayingHistoryRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@Sql("/dbclean.sql")
@ActiveProfiles("integration-test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@org.springframework.test.context.TestPropertySource(properties = "hearit.playing-history.buffer-type=memory")
@Import({DbHelper.class, TestJpaAuditingConfig.class, DataSourceConfig.class, PlayingHistoryMapBuffer.class,
        PlayingHistoryCommandRepository.class, PlayingHistoryService.class, UserInfoService.class})
class PlayingHistoryServiceTest {

    @Autowired
    DbHelper dbHelper;

    @Autowired
    PlayingHistoryRepository playingHistoryRepository;

    @Autowired
    PlayingHistoryBuffer playingHistoryBuffer;

    @Autowired
    PlayingHistoryCommandRepository playingHistoryCommandRepository;

    @Autowired
    HearitRepository hearitRepository;

    @Autowired
    PlayingHistoryService playingHistoryService;

    @Nested
    @DisplayName("최근 재생 기록 조회")
    class GetRecentPlayingHistoryTest {

        @Test
        @DisplayName("회원은 최근 재생 기록을 조회할 수 있다.")
        void getRecentPlayingHistory_whenMember() {
            // given
            Member member = dbHelper.insertMember(TestFixture.createFixedMember());
            UserInfo memberInfo = RequestUser.member(member.getId()).getUserInfo();
            Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
            Hearit hearit1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
            Hearit hearit2 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

            LocalDateTime baseTime = LocalDateTime.of(2025, 1, 1, 0, 0);
            dbHelper.insertPlayingHistoryAt(new PlayingHistory(member.getUuid(), hearit1, 10),
                    baseTime.minusMinutes(10));
            dbHelper.insertPlayingHistoryAt(new PlayingHistory(member.getUuid(), hearit2, 20), baseTime);

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
        @DisplayName("비회원은 최근 재생 기록을 조회할 수 있다.")
        void getRecentPlayingHistory_whenGuest() {
            // given
            UserInfo guestInfo = new UserInfo(null, UUID.randomUUID().toString());
            Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
            Hearit hearit1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
            Hearit hearit2 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

            LocalDateTime baseTime = LocalDateTime.of(2025, 1, 1, 0, 0);
            dbHelper.insertPlayingHistoryAt(new PlayingHistory(guestInfo.getGuestId(), hearit1, 10),
                    baseTime.minusMinutes(10));
            dbHelper.insertPlayingHistoryAt(new PlayingHistory(guestInfo.getGuestId(), hearit2, 20),
                    baseTime);

            // when
            List<RecentlyPlayedHearitResponse> result = playingHistoryService.getRecentPlayingHistory(guestInfo);

            // then
            assertAll(
                    () -> assertThat(result).hasSize(2),
                    () -> assertThat(result.get(0).id()).isEqualTo(hearit2.getId()),
                    () -> assertThat(result.get(1).id()).isEqualTo(hearit1.getId())
            );
        }
    }

    @Nested
    @DisplayName("재생 기록 저장")
    class AddPlayingHistoryTest {

        @Test
        @DisplayName("로그인한 회원은 재생기록을 저장할 수 있다.")
        void addPlayHistory_Member() {
            // given
            Member member = dbHelper.insertMember(TestFixture.createFixedMember());
            Category category = dbHelper.insertCategory(new Category("name", "#000000"));
            Hearit hearit = dbHelper.insertHearit(createHearitWith(100, category));
            PlayingHistoryRequest request = new PlayingHistoryRequest(hearit.getId(), 100L, 200L);

            // when
            playingHistoryService.addPlayingHistory(TestFixture.createFixedMemberUserInfo(member), request);
            playingHistoryBuffer.flush();

            // then
            List<PlayingHistory> playingHistories = playingHistoryRepository.findAll();
            assertAll(
                    () -> assertThat(playingHistories.size()).isEqualTo(1),
                    () -> assertThat(playingHistories.getFirst().getUserUuid()).isEqualTo(member.getUuid()),
                    () -> assertThat(playingHistories.getFirst().getHearitId()).isEqualTo(hearit.getId())
            );
        }

        @Test
        @DisplayName("비회원은 재생기록을 저장할 수 있다.")
        void addPlayHistory_Guest() {
            // given
            Category category = dbHelper.insertCategory(new Category("name", "#000000"));
            Hearit hearit = dbHelper.insertHearit(createHearitWith(100, category));
            PlayingHistoryRequest request = new PlayingHistoryRequest(hearit.getId(), 100L, 200L);
            String guestUuid = UUID.randomUUID().toString();

            // when
            playingHistoryService.addPlayingHistory(TestFixture.createGuestUserInfo(guestUuid), request);
            playingHistoryBuffer.flush();

            // then
            List<PlayingHistory> playingHistories = playingHistoryRepository.findAll();
            assertAll(
                    () -> assertThat(playingHistories.size()).isEqualTo(1),
                    () -> assertThat(playingHistories.getFirst().getUserUuid()).isEqualTo(guestUuid),
                    () -> assertThat(playingHistories.getFirst().getHearitId()).isEqualTo(hearit.getId())
            );
        }
    }

    @Nested
    @DisplayName("재생 기록 수정")
    class ModifyPlayingHistoryTest {

        @Test
        @DisplayName("로그인한 회원은 재생기록을 수정할 수 있다.")
        void modifyPlayHistory_Member() {
            // given
            Member member = dbHelper.insertMember(TestFixture.createFixedMember());
            Category category = dbHelper.insertCategory(new Category("name", "#000000"));
            Hearit hearit = dbHelper.insertHearit(createHearitWith(100, category));
            dbHelper.insertPlayingHistory(new PlayingHistory(member.getUuid(), hearit, 10_000));
            PlayingHistoryRequest request = new PlayingHistoryRequest(hearit.getId(), 50_000L, 200L);

            // when
            playingHistoryService.addPlayingHistory(TestFixture.createFixedMemberUserInfo(member), request);
            playingHistoryBuffer.flush();

            // then
            List<PlayingHistory> playingHistories = playingHistoryRepository.findAll();
            assertAll(
                    () -> assertThat(playingHistories.size()).isEqualTo(1),
                    () -> assertThat(playingHistories.getFirst().getUserUuid()).isEqualTo(member.getUuid()),
                    () -> assertThat(playingHistories.getFirst().getHearitId()).isEqualTo(hearit.getId()),
                    () -> assertThat(playingHistories.getFirst().getLastPlayTime()).isEqualTo(50_000L)
            );
        }

        @Test
        @Commit
        @DisplayName("비회원은 재생기록을 수정할 수 있다.")
        void modifyPlayHistory_Guest() {
            // given
            UserInfo guestUserInfo = TestFixture.createGuestUserInfo(UUID.randomUUID().toString());
            Category category = dbHelper.insertCategory(new Category("name", "#000000"));
            Hearit hearit = dbHelper.insertHearit(createHearitWith(100, category));
            dbHelper.insertPlayingHistory(new PlayingHistory(guestUserInfo.getGuestId(), hearit, 10_000));
            PlayingHistoryRequest request = new PlayingHistoryRequest(hearit.getId(), 50_000L, 200L);

            // when
            playingHistoryService.addPlayingHistory(guestUserInfo, request);
            playingHistoryBuffer.flush();

            // then
            List<PlayingHistory> playingHistories = playingHistoryRepository.findAll();
            assertAll(
                    () -> assertThat(playingHistories.size()).isEqualTo(1),
                    () -> assertThat(playingHistories.getFirst().getUserUuid()).isEqualTo(guestUserInfo.getGuestId()),
                    () -> assertThat(playingHistories.getFirst().getHearitId()).isEqualTo(hearit.getId()),
                    () -> assertThat(playingHistories.getFirst().getLastPlayTime()).isEqualTo(50_000L)
            );
        }
    }

    @Test
    @DisplayName("존재하지 않는 히어릿에 대한 재생기록을 저장할 수 없다.")
    void checkHearit() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        PlayingHistoryRequest request = new PlayingHistoryRequest(1L, 100L, 200L);

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
