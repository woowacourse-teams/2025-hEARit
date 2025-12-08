package com.onair.hearit.core.infrastructure.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.Member;
import com.onair.hearit.core.domain.PlayingHistory;
import com.onair.hearit.core.fixture.DbHelper;
import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("fake-test")
@Import({DbHelper.class, TestJpaAuditingConfig.class})
class PlayingHistoryRepositoryTest {

    @Autowired
    private DbHelper dbHelper;

    @Autowired
    private PlayingHistoryRepository playingHistoryRepository;

    @Nested
    @DisplayName("유저의 재생 기록을 원하는 개수만큼 최신순으로 정렬")
    class findByUserUuidOrderByUpdatedAtDesc {

        @Test
        @DisplayName("회원의 재생 기록을 업데이트 날짜를 기준으로 내림차순 정렬하여 10개 조회한다.")
        void findByUserUuidOrderByUpdatedAtDesc_Member() {
            // given
            Member member1 = dbHelper.insertMember(TestFixture.createFixedMember());
            Member member2 = dbHelper.insertMember(TestFixture.createFixedMember());
            Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
            Hearit hearit1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
            Hearit hearit2 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

            dbHelper.insertPlayingHistoryAt(new PlayingHistory(member1.getUuid(), hearit1, 10),
                    LocalDateTime.now().minusMinutes(10));
            dbHelper.insertPlayingHistoryAt(new PlayingHistory(member1.getUuid(), hearit2, 20),
                    LocalDateTime.now().minusMinutes(1));
            dbHelper.insertPlayingHistoryAt(new PlayingHistory(member2.getUuid(), hearit2, 20),
                    LocalDateTime.now()); // 다른 멤버

            // when
            List<PlayingHistory> result = playingHistoryRepository.findByUserUuidOrderByUpdatedAtDesc(member1.getUuid(),
                    10);

            // then
            assertAll(() -> {
                assertThat(result).hasSize(2);
                assertThat(result.get(0).getHearitId()).isEqualTo(hearit2.getId());
                assertThat(result.get(1).getHearitId()).isEqualTo(hearit1.getId());
            });
        }

        @Test
        @DisplayName("회원의 재생 기록을 업데이트 날짜를 기준으로 내림차순 정렬하여 10개 조회한다.")
        void findByUserUuidOrderByUpdatedAtDesc_Guest() {
            // given
            String guestUuid1 = UUID.randomUUID().toString();
            String guestUuid2 = UUID.randomUUID().toString();
            Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
            Hearit hearit1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
            Hearit hearit2 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

            dbHelper.insertPlayingHistoryAt(new PlayingHistory(guestUuid1, hearit1, 10),
                    LocalDateTime.now().minusMinutes(10));
            dbHelper.insertPlayingHistoryAt(new PlayingHistory(guestUuid1, hearit2, 20),
                    LocalDateTime.now().minusMinutes(1));
            dbHelper.insertPlayingHistoryAt(new PlayingHistory(guestUuid2, hearit2, 20),
                    LocalDateTime.now()); // 다른 게스트

            // when
            List<PlayingHistory> result = playingHistoryRepository.findByUserUuidOrderByUpdatedAtDesc(guestUuid1, 10);

            // then
            assertAll(() -> {
                assertThat(result).hasSize(2);
                assertThat(result.get(0).getHearitId()).isEqualTo(hearit2.getId());
                assertThat(result.get(1).getHearitId()).isEqualTo(hearit1.getId());
            });
        }
    }
}
