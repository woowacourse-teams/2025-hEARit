package com.onair.hearit.app.explore.application.scorefactor;

import static org.assertj.core.api.Assertions.assertThat;

import com.onair.hearit.app.fixture.DbHelper;
import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.Member;
import com.onair.hearit.core.domain.UserType;
import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import com.onair.hearit.core.infrastructure.jpa.PlayingHistoryRepository;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import({DbHelper.class, TestJpaAuditingConfig.class})
@ActiveProfiles("fake-test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
class PlayingHistoryScoreFactorTest {

    @Autowired
    private DbHelper dbHelper;

    @Autowired
    private PlayingHistoryRepository playingHistoryRepository;

    private PlayingHistoryScoreFactor playingHistoryScoreFactor;

    @BeforeEach
    void setUp() {
        playingHistoryScoreFactor = new PlayingHistoryScoreFactor(playingHistoryRepository);
    }

    @Test
    @DisplayName("회원은 재생기록 점수를 받는다")
    void memberSupport() {
        // when and then
        assertThat(playingHistoryScoreFactor.isSupported(UserType.MEMBER)).isTrue();
    }

    @Test
    @DisplayName("비회원은 재생기록 점수를 받는다")
    void guestNotSupport() {
        // when and then
        assertThat(playingHistoryScoreFactor.isSupported(UserType.GUEST)).isTrue();
    }

    @Test
    @DisplayName("재생기록이 가장 많은 카테고리 히어릿은 1.0 점을 받는다")
    void mostPlayedCategories() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());

        Category firstCategory = TestFixture.createFixedCategory();
        Category lastCategory = TestFixture.createFixedCategory();
        dbHelper.insertCategory(firstCategory);
        dbHelper.insertCategory(lastCategory);

        Hearit firstHearit1 = TestFixture.createFixedHearitWith(firstCategory);
        Hearit firstHearit2 = TestFixture.createFixedHearitWith(firstCategory);
        dbHelper.insertHearit(firstHearit1);
        dbHelper.insertHearit(firstHearit2);
        Hearit lastHearit1 = TestFixture.createFixedHearitWith(lastCategory);
        dbHelper.insertHearit(lastHearit1);

        dbHelper.insertPlayingHistory(TestFixture.createFixedPlayHistory(member, firstHearit1));
        dbHelper.insertPlayingHistory(TestFixture.createFixedPlayHistory(member, firstHearit2));
        dbHelper.insertPlayingHistory(TestFixture.createFixedPlayHistory(member, lastHearit1));

        List<Hearit> hearits = List.of(firstHearit1, firstHearit2, lastHearit1);

        // when
        Map<Long, Double> result = playingHistoryScoreFactor.calculate(member.getUuid(), hearits);

        // then
        assertThat(result).containsEntry(firstHearit1.getId(), 1.0);
    }

    @Test
    @DisplayName("재생기록이 없는 카테고리 히어릿은 0 점을 받는다")
    void notPlayedCategories() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());

        Category firstCategory = TestFixture.createFixedCategory();
        Category lastCategory = TestFixture.createFixedCategory();
        dbHelper.insertCategory(firstCategory);
        dbHelper.insertCategory(lastCategory);

        Hearit firstHearit1 = TestFixture.createFixedHearitWith(firstCategory);
        dbHelper.insertHearit(firstHearit1);
        Hearit lastHearit1 = TestFixture.createFixedHearitWith(lastCategory);
        dbHelper.insertHearit(lastHearit1);
        dbHelper.insertPlayingHistory(TestFixture.createFixedPlayHistory(member, firstHearit1));

        List<Hearit> hearits = List.of(firstHearit1, lastHearit1);

        // when
        Map<Long, Double> result = playingHistoryScoreFactor.calculate(member.getUuid(), hearits);

        // then
        assertThat(result).containsEntry(lastHearit1.getId(), 0.0);
    }

    @Test
    @DisplayName("카테고리 별로 (자신의재생기록수/최다재생기록수) 로 점수를 받는다.")
    void calculatePlayHistoryScoreFactor() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());

        Category firstCategory = TestFixture.createFixedCategory();
        Category lastCategory = TestFixture.createFixedCategory();
        dbHelper.insertCategory(firstCategory);
        dbHelper.insertCategory(lastCategory);

        Hearit firstHearit1 = TestFixture.createFixedHearitWith(firstCategory);
        Hearit firstHearit2 = TestFixture.createFixedHearitWith(firstCategory);
        Hearit firstHearit3 = TestFixture.createFixedHearitWith(firstCategory);
        Hearit firstHearit4 = TestFixture.createFixedHearitWith(firstCategory);
        dbHelper.insertHearit(firstHearit1);
        dbHelper.insertHearit(firstHearit2);
        dbHelper.insertHearit(firstHearit3);
        dbHelper.insertHearit(firstHearit4);
        Hearit lastHearit1 = TestFixture.createFixedHearitWith(lastCategory);
        Hearit lastHearit2 = TestFixture.createFixedHearitWith(lastCategory);
        dbHelper.insertHearit(lastHearit1);
        dbHelper.insertHearit(lastHearit2);

        dbHelper.insertPlayingHistory(TestFixture.createFixedPlayHistory(member, firstHearit1));
        dbHelper.insertPlayingHistory(TestFixture.createFixedPlayHistory(member, firstHearit2));
        dbHelper.insertPlayingHistory(TestFixture.createFixedPlayHistory(member, firstHearit3));
        dbHelper.insertPlayingHistory(TestFixture.createFixedPlayHistory(member, firstHearit4));
        dbHelper.insertPlayingHistory(TestFixture.createFixedPlayHistory(member, lastHearit1));
        dbHelper.insertPlayingHistory(TestFixture.createFixedPlayHistory(member, lastHearit2));

        List<Hearit> hearits = List.of(firstHearit1, firstHearit2, firstHearit3, firstHearit4, lastHearit1,
                lastHearit2);

        // when
        Map<Long, Double> result = playingHistoryScoreFactor.calculate(member.getUuid(), hearits);

        // then
        assertThat(result)
                .containsEntry(firstHearit1.getId(), 1.0)
                .containsEntry(firstHearit2.getId(), 1.0)
                .containsEntry(firstHearit3.getId(), 1.0)
                .containsEntry(firstHearit4.getId(), 1.0)
                .containsEntry(lastHearit1.getId(), 0.5)
                .containsEntry(lastHearit2.getId(), 0.5);
    }

    @Test
    @DisplayName("카테고리 선호도와 관계 없이 다 들은 히어릿은 0 점을 받는다")
    void isFinishedHearit() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());

        Category firstCategory = TestFixture.createFixedCategory();
        dbHelper.insertCategory(firstCategory);

        Hearit firstHearit1 = TestFixture.createFixedHearitWith(firstCategory);
        Hearit firstHearit2 = TestFixture.createFixedHearitWith(firstCategory);
        dbHelper.insertHearit(firstHearit1);
        dbHelper.insertHearit(firstHearit2);

        dbHelper.insertPlayingHistory(TestFixture.createFixedPlayHistory(member, firstHearit1));
        dbHelper.insertPlayingHistory(TestFixture.createFixedFinishedPlayHistory(member, firstHearit2));

        List<Hearit> hearits = List.of(firstHearit1, firstHearit2);

        // when
        Map<Long, Double> result = playingHistoryScoreFactor.calculate(member.getUuid(), hearits);

        // then
        assertThat(result).containsEntry(firstHearit2.getId(), 0.0);
    }
}
