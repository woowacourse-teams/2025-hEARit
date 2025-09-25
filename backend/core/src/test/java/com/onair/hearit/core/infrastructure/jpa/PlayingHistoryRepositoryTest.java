package com.onair.hearit.core.infrastructure.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.Member;
import com.onair.hearit.domain.PlayingHistory;
import com.onair.hearit.core.fixture.DbHelper;
import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import com.onair.hearit.infrastructure.jpa.PlayingHistoryRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
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

    @Test
    @DisplayName("회원의 재생 기록을 업데이트 날짜를 기준으로 내림차순 정렬하여 10개 조회한다.")
    void findByMemberIdOrderByUpdatedAtDesc() throws InterruptedException {
        // given
        Member member1 = dbHelper.insertMember(TestFixture.createFixedMember());
        Member member2 = dbHelper.insertMember(TestFixture.createFixedMember());
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Hearit hearit2 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        dbHelper.insertPlayingHistory(new PlayingHistory(member1.getId(), hearit1, 10));
        Thread.sleep(10);
        dbHelper.insertPlayingHistory(new PlayingHistory(member1.getId(), hearit2, 20));
        Thread.sleep(10);
        dbHelper.insertPlayingHistory(new PlayingHistory(member2.getId(), hearit2, 20));

        // when
        List<PlayingHistory> result = playingHistoryRepository.findByMemberIdOrderByUpdatedAtDesc(member1.getId(), 10);

        // then
        assertAll(() -> {
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getHearitId()).isEqualTo(hearit2.getId());
            assertThat(result.get(1).getHearitId()).isEqualTo(hearit1.getId());
        });
    }
}
