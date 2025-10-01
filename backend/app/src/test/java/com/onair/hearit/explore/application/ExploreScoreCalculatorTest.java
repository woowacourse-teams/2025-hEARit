package com.onair.hearit.explore.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;

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
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@Sql("/dbclean.sql")
@Import({DbHelper.class, TestJpaAuditingConfig.class, RandomScoreFactor.class, RecencyScoreFactor.class,
        BookmarkScoreFactor.class, ExploreScoreCalculator.class})
@ActiveProfiles("integration-test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
class ExploreScoreCalculatorTest {

    @MockitoBean
    private RandomNumberGenerator randomNumberGenerator;

    @Autowired
    private DbHelper dbHelper;

    @Autowired
    private ExploreScoreCalculator exploreScoreCalculator;


    @DisplayName("회원은 북마크·최신성·랜덤 점수를 모두 합산한다")
    @Test
    void calculateTotalScoresForMember() {
        // given
        // 랜덤 점수를 1.0점으로 고정
        double fixedRandomDouble = 0.1d;
        double fixedRandomScore = fixedRandomDouble * 10;
        given(randomNumberGenerator.nextDouble()).willReturn(fixedRandomDouble);

        Category category1 = dbHelper.insertCategory(new Category("Java", "#112233"));
        Category category2 = dbHelper.insertCategory(new Category("Android", "#445566"));
        Category category3 = dbHelper.insertCategory(new Category("Kotlin", "#778899"));
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        LocalDateTime now = LocalDateTime.now();

        // 북마크 이력용 데이터 생성 (카테고리1: 3개, 카테고리2: 1개)
        dbHelper.insertBookmark(new Bookmark(member, dbHelper.insertHearitAt(createHearit(category1), now)));
        dbHelper.insertBookmark(new Bookmark(member, dbHelper.insertHearitAt(createHearit(category1), now)));
        dbHelper.insertBookmark(new Bookmark(member, dbHelper.insertHearitAt(createHearit(category1), now)));
        dbHelper.insertBookmark(new Bookmark(member, dbHelper.insertHearitAt(createHearit(category2), now)));

        // 점수 검증 대상 데이터 생성
        Hearit hearitWithHighBookmark = dbHelper.insertHearitAt(createHearit(category1), now);
        Hearit hearitWithLowBookmark = dbHelper.insertHearitAt(createHearit(category2), now.minusDays(4));
        Hearit hearitWithNoBookmark = dbHelper.insertHearitAt(createHearit(category3), now.minusDays(60));

        // when
        Map<Long, Double> scores = exploreScoreCalculator.calculateTotalScores(member.getUuid(), UserType.MEMBER);

        // then
        // 예상 점수 계산 (회원 점수 = 북마크 점수 + 최신성 점수 + 랜덤 점수)
        double highBookmarkScore = (3.0 / 4.0) * 30.0; // 22.5
        double lowBookmarkScore = (1.0 / 4.0) * 30.0;  // 7.5

        double score1 = highBookmarkScore + calculateRecencyScore(now, now)
                + fixedRandomScore;                   // 22.5 + 20.0 + 1.0 = 43.5
        double score2 = lowBookmarkScore + calculateRecencyScore(now.minusDays(4), now)
                + fixedRandomScore;      // 7.5 + 18.0 + 1.0 = 26.5
        double score3 = 0.0 + calculateRecencyScore(now.minusDays(60), now)
                + fixedRandomScore;                   // 0.0 + 0.0 + 1.0 = 1.0

        assertAll(
                () -> assertThat(scores).hasSize(7), // 북마크 이력용(4개) + 검증 대상(3개)
                () -> assertThat(scores.get(hearitWithHighBookmark.getId())).isEqualTo(score1),
                () -> assertThat(scores.get(hearitWithLowBookmark.getId())).isEqualTo(score2),
                () -> assertThat(scores.get(hearitWithNoBookmark.getId())).isEqualTo(score3)
        );
    }

    @DisplayName("게스트는 북마크 점수를 제외하고 최신성·랜덤 점수만 합산한다")
    @Test
    void calculateTotalScoresForGuest() {
        // given
        String guestUuid = UUID.randomUUID().toString();

        // 랜덤 점수를 1.0점으로 고정
        double fixedRandomDouble = 0.1d;
        double fixedRandomScore = fixedRandomDouble * 10;
        given(randomNumberGenerator.nextDouble()).willReturn(fixedRandomDouble);

        Category category1 = dbHelper.insertCategory(new Category("Java", "#112233"));
        Category category2 = dbHelper.insertCategory(new Category("Android", "#445566"));
        LocalDateTime now = LocalDateTime.now();

        Hearit hearit1 = dbHelper.insertHearitAt(createHearit(category1), now);
        Hearit hearit2 = dbHelper.insertHearitAt(createHearit(category2), now.minusDays(4));

        // when
        Map<Long, Double> scores = exploreScoreCalculator.calculateTotalScores(guestUuid, UserType.GUEST);

        // then
        // 예상 점수 계산 (게스트 점수 = 최신성 점수 + 랜덤 점수)
        double score1 = calculateRecencyScore(now, now) + fixedRandomScore;             // 20.0 + 1.0 = 21.0
        double score2 = calculateRecencyScore(now.minusDays(4), now) + fixedRandomScore; // 18.0 + 1.0 = 19.0

        assertAll(
                () -> assertThat(scores).hasSize(2),
                () -> assertThat(scores.get(hearit1.getId())).isEqualTo(score1),
                () -> assertThat(scores.get(hearit2.getId())).isEqualTo(score2)
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

    private double calculateRecencyScore(LocalDateTime createdAt, LocalDateTime now) {
        long daysPassed = Duration.between(createdAt.toLocalDate().atStartOfDay(), now.toLocalDate().atStartOfDay())
                .toDays();
        double recencyScore = 20.0 - (daysPassed * 0.5);
        return Math.max(0.0, recencyScore);
    }
}
