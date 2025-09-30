package com.onair.hearit.explore.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;

import com.onair.hearit.common.TestClock;
import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import com.onair.hearit.domain.Bookmark;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.Member;
import com.onair.hearit.domain.Source;
import com.onair.hearit.domain.UserType;
import com.onair.hearit.explore.application.scorefactor.BookmarkScoreFactor;
import com.onair.hearit.explore.application.scorefactor.RandomNumberGenerator;
import com.onair.hearit.explore.application.scorefactor.RandomScoreFactor;
import com.onair.hearit.explore.application.scorefactor.RecencyScoreFactor;
import com.onair.hearit.fixture.DbHelper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@Sql("/dbclean.sql")
@Import({DbHelper.class, TestJpaAuditingConfig.class, RandomScoreFactor.class, RecencyScoreFactor.class,
        BookmarkScoreFactor.class, ExploreScoreCalculator.class})
@ActiveProfiles("integration-test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
class ExploreScoreCalculatorTest {

    private static final double FIXED_RANDOM_SCORE = 1.0;

    @MockBean
    private RandomNumberGenerator randomNumberGenerator;

    @Autowired
    private DbHelper dbHelper;

    @Autowired
    private ExploreScoreCalculator exploreScoreCalculator;

    @AfterEach
    void tearDown() {
        TestClock.unfreeze();
    }

    @DisplayName("회원은 북마크·최신성·랜덤 점수를 모두 합산한다")
    @Test
    void calculateTotalScoresForMemberWithRealFactors() {
        // given
        given(randomNumberGenerator.nextDouble()).willReturn(0.1d);
        Category category1 = dbHelper.insertCategory(new Category("Java", "#112233"));
        Category category2 = dbHelper.insertCategory(new Category("Android", "#445566"));
        Category category3 = dbHelper.insertCategory(new Category("Kotlin", "#778899"));
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());

        // -- 북마크 이력용 Hearit들 --
        TestClock.freezeAt(LocalDateTime.now());
        Hearit hearit1 = dbHelper.insertHearit(createHearit(category1));
        Hearit hearit2 = dbHelper.insertHearit(createHearit(category1));
        Hearit hearit3 = dbHelper.insertHearit(createHearit(category1));
        Hearit hearit4 = dbHelper.insertHearit(createHearit(category2));

        //  카테고리별 북마크 - category1 : 3개, category2: 1개
        dbHelper.insertBookmark(new Bookmark(member, hearit1));
        dbHelper.insertBookmark(new Bookmark(member, hearit2));
        dbHelper.insertBookmark(new Bookmark(member, hearit3));
        dbHelper.insertBookmark(new Bookmark(member, hearit4));

        // -- 점수 계산 대상 Hearit들 --
        // 최신성 20, 카테고리 22.5
        TestClock.freezeAt(LocalDateTime.now());
        Hearit hearit5 = dbHelper.insertHearit(createHearit(category1));
        // 최신성 18, 카테고리 7.5
        TestClock.freezeAt(LocalDateTime.now().minusDays(4));
        Hearit hearit6 = dbHelper.insertHearit(createHearit(category2));
        // 최신성 0, 카테고리 0
        TestClock.freezeAt(LocalDateTime.now().minusDays(60));
        Hearit hearit7 = dbHelper.insertHearit(createHearit(category3));

        // when
        Map<Long, Double> scores = exploreScoreCalculator.calculateTotalScores(member.getUuid(), UserType.MEMBER);

        // then
        assertAll(
                () -> assertThat(scores).hasSize(7),
                () -> assertThat(scores.get(hearit5.getId())).isEqualTo(22.5 + 20.0 + FIXED_RANDOM_SCORE),
                () -> assertThat(scores.get(hearit6.getId())).isEqualTo(7.5 + 18.0 + FIXED_RANDOM_SCORE),
                () -> assertThat(scores.get(hearit7.getId())).isEqualTo(0.0 + 0.0 + FIXED_RANDOM_SCORE)
        );
    }

    @DisplayName("게스트는 북마크 점수를 제외하고 합산한다")
    @Test
    void calculateTotalScoresForGuestWithRealFactors() {
        given(randomNumberGenerator.nextDouble()).willReturn(0.1d);
        // given
        Category category1 = dbHelper.insertCategory(new Category("Java", "#112233"));
        Category category2 = dbHelper.insertCategory(new Category("Android", "#445566"));
        Category category3 = dbHelper.insertCategory(new Category("Kotlin", "#778899"));
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());

        // 북마크 이력 Hearit (게스트 점수엔 반영 안 됨)
        TestClock.freezeAt(LocalDateTime.now());
        Hearit hearit1 = dbHelper.insertHearit(createHearit(category1));
        dbHelper.insertBookmark(new Bookmark(member, hearit1));

        // -- 점수 계산 대상 Hearit들 --
        TestClock.freezeAt(LocalDateTime.now());
        Hearit hearit2 = dbHelper.insertHearit(createHearit(category1));              // ֽż 20
        TestClock.freezeAt(LocalDateTime.now().minusDays(4));
        Hearit hearit3 = dbHelper.insertHearit(createHearit(category2)); // ֽż 18
        TestClock.freezeAt(LocalDateTime.now().minusDays(60));
        Hearit hearit4 = dbHelper.insertHearit(createHearit(category3));// ֽż 0

        String guestUuid = UUID.randomUUID().toString();

        // when
        Map<Long, Double> scores = exploreScoreCalculator.calculateTotalScores(guestUuid, UserType.GUEST);

        // then
        assertAll(
                () -> assertThat(scores).hasSize(4),
                () -> assertThat(scores.get(hearit2.getId())).isEqualTo(20.0 + FIXED_RANDOM_SCORE),
                () -> assertThat(scores.get(hearit3.getId())).isEqualTo(18.0 + FIXED_RANDOM_SCORE),
                () -> assertThat(scores.get(hearit4.getId())).isEqualTo(0.0 + FIXED_RANDOM_SCORE)
        );
    }

    private Hearit createHearit(Category category) {
        return new Hearit(
                "title",
                "summary",
                500,
                "/hearit/audio/original/ORG_bf7c513e-579e-4224-8505-3824bb22ed01.mp3",
                "/hearit/audio/short/SHR_bf7c513e-579e-4224-8505-3824bb22ed01.mp3",
                "/hearit/script/SCR_bf7c513e-579e-4224-8505-3824bb22ed01.json",
                List.of(new Source("이 컨텐츠는 쿠버네티스 공식 문서 (저작자: The Kubernetes Authors)를 참고하여 만들어졌습니다.",
                        "https://example.com/1")),
                category
        );
    }
}
