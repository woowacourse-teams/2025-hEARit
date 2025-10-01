package com.onair.hearit.explore.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;

import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import com.onair.hearit.domain.Bookmark;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Member;
import com.onair.hearit.domain.UserType;
import com.onair.hearit.explore.application.scorefactor.BookmarkScoreFactor;
import com.onair.hearit.explore.application.scorefactor.RandomNumberGenerator;
import com.onair.hearit.explore.application.scorefactor.RandomScoreFactor;
import com.onair.hearit.explore.application.scorefactor.RecencyScoreFactor;
import com.onair.hearit.fixture.DbHelper;
import com.onair.hearit.infrastructure.jdbc.ExploreScoreCommandRepository;
import java.time.Duration;
import java.time.LocalDateTime;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@Sql("/dbclean.sql")
@Import({DbHelper.class, TestJpaAuditingConfig.class, ExploreScoreCommandRepository.class,
        RandomScoreFactor.class, RecencyScoreFactor.class, BookmarkScoreFactor.class, ExploreScoreCalculator.class})
@ActiveProfiles("integration-test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
class ExploreScoreRefresherTest {

    @MockitoBean
    private RandomNumberGenerator randomNumberGenerator;

    @Autowired
    private DbHelper dbHelper;

    @Autowired
    private ExploreScoreCommandRepository exploreScoreCommandRepository;

    @Autowired
    private ExploreScoreCalculator exploreScoreCalculator;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ExploreScoreRefresher exploreScoreRefresher;

    @BeforeEach
    void setUp() {
        exploreScoreRefresher = new ExploreScoreRefresher(exploreScoreCalculator, exploreScoreCommandRepository);
    }

    @DisplayName("cursorId가 0이 아니면 갱신하지 않는다")
    @Test
    void skipRefreshingWhenCursorIsNotZero() {
        // given
        String guestUuid = UUID.randomUUID().toString();
        long cursorId = 1L;

        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        // when
        exploreScoreRefresher.refreshIfNeeded(cursorId, guestUuid, UserType.GUEST);

        // then
        assertThat(findExploreScores(guestUuid)).isEmpty();
    }

    @DisplayName("cursorId가 0이고 비회원이면 최신성, 랜덤 점수를 합산하여 점수를 갱신한다")
    @Test
    void refreshScoresWhenCursorIsZeroForGuest() {
        // given
        String guestUuid = UUID.randomUUID().toString();
        long cursorId = 0L;

        // 랜덤 점수를 1.0점으로 고정
        double fixedRandomDouble = 0.1d;
        double fixedRandomScore = fixedRandomDouble * 10;
        given(randomNumberGenerator.nextDouble()).willReturn(fixedRandomDouble);

        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        LocalDateTime now = LocalDateTime.now();
        dbHelper.insertHearitAt(TestFixture.createFixedHearitWith(category), now);
        dbHelper.insertHearitAt(TestFixture.createFixedHearitWith(category), now.minusDays(2));
        dbHelper.insertHearitAt(TestFixture.createFixedHearitWith(category), now.minusDays(5));

        // when
        exploreScoreRefresher.refreshIfNeeded(cursorId, guestUuid, UserType.GUEST);

        // then
        List<ExploreScoreRow> rows = findExploreScores(guestUuid);

        // 예상 점수 계산 (게스트 점수 = 최신성 점수 + 랜덤 점수)
        double scoreForNewest = calculateRecencyScore(now, now) + fixedRandomScore;          // 20.0 + 1.0 = 21.0
        double scoreForTwoDaysAgo =
                calculateRecencyScore(now.minusDays(2), now) + fixedRandomScore;  // 19.0 + 1.0 = 20.0
        double scoreForFiveDaysAgo =
                calculateRecencyScore(now.minusDays(5), now) + fixedRandomScore; // 17.5 + 1.0 = 18.5

        assertAll(
                () -> assertThat(rows).hasSize(3),
                // 점수가 높은 순으로 정렬(cursor_id 순서)되므로, 순서대로 점수를 검증
                () -> assertThat(rows.get(0).score()).isEqualTo(scoreForNewest),      // 1위
                () -> assertThat(rows.get(1).score()).isEqualTo(scoreForTwoDaysAgo),  // 2위
                () -> assertThat(rows.get(2).score()).isEqualTo(scoreForFiveDaysAgo)   // 3위
        );
    }

    @DisplayName("cursorId가 0이고 회원이면 최신성, 북마크, 랜덤 점수를 합산하여 점수를 갱신한다")
    @Test
    void refreshScoresWhenCursorIsZeroForMember() {
        // given
        long cursorId = 0L;

        // 랜덤 점수를 1.0점으로 고정
        double fixedRandomDouble = 0.1d;
        double fixedRandomScore = fixedRandomDouble * 10;
        given(randomNumberGenerator.nextDouble()).willReturn(fixedRandomDouble);

        Category category1 = dbHelper.insertCategory(new Category("Java", "#112233"));
        Category category2 = dbHelper.insertCategory(new Category("Android", "#445566"));
        Category category3 = dbHelper.insertCategory(new Category("Kotlin", "#778899"));
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        LocalDateTime now = LocalDateTime.now();

        // 북마크 이력용 Hearit 생성 (카테고리1: 3개, 카테고리2: 1개)
        dbHelper.insertBookmark(
                new Bookmark(member, dbHelper.insertHearitAt(TestFixture.createFixedHearitWith(category1), now)));
        dbHelper.insertBookmark(
                new Bookmark(member, dbHelper.insertHearitAt(TestFixture.createFixedHearitWith(category1), now)));
        dbHelper.insertBookmark(
                new Bookmark(member, dbHelper.insertHearitAt(TestFixture.createFixedHearitWith(category1), now)));
        dbHelper.insertBookmark(
                new Bookmark(member, dbHelper.insertHearitAt(TestFixture.createFixedHearitWith(category2), now)));

        // 점수 검증 대상 Hearit 생성
        dbHelper.insertHearitAt(TestFixture.createFixedHearitWith(category1), now);
        dbHelper.insertHearitAt(TestFixture.createFixedHearitWith(category2), now.minusDays(4));
        dbHelper.insertHearitAt(TestFixture.createFixedHearitWith(category3), now.minusDays(60));

        // when
        exploreScoreRefresher.refreshIfNeeded(cursorId, member.getUuid(), UserType.MEMBER);

        // then
        List<ExploreScoreRow> rows = findExploreScores(member.getUuid());

        // 예상 점수 계산 (회원 점수 = 최신성 점수 + 북마크 점수 + 랜덤 점수)
        double highBookmarkScore = (3.0 / 4.0) * 30.0; // 22.5
        double lowBookmarkScore = (1.0 / 4.0) * 30.0;  // 7.5

        double scoreGroup1 = highBookmarkScore + calculateRecencyScore(now, now)
                + fixedRandomScore;                   // 22.5 + 20.0 + 1.0 = 43.5
        double scoreGroup2 = lowBookmarkScore + calculateRecencyScore(now, now)
                + fixedRandomScore;                    // 7.5 + 20.0 + 1.0 = 28.5
        double scoreGroup3 = lowBookmarkScore + calculateRecencyScore(now.minusDays(4), now)
                + fixedRandomScore;      // 7.5 + 18.0 + 1.0 = 26.5
        double scoreGroup4 = 0.0 + calculateRecencyScore(now.minusDays(60), now)
                + fixedRandomScore;                   // 0.0 + 0.0 + 1.0 = 1.0

        assertAll(
                () -> assertThat(rows).hasSize(7),
                // 1~4위 (4개): 카테고리1, 생성일 now -> 43.5점
                () -> assertThat(rows.get(0).score()).isEqualTo(scoreGroup1),
                () -> assertThat(rows.get(1).score()).isEqualTo(scoreGroup1),
                () -> assertThat(rows.get(2).score()).isEqualTo(scoreGroup1),
                () -> assertThat(rows.get(3).score()).isEqualTo(scoreGroup1),
                // 5위 (1개): 카테고리2, 생성일 now -> 28.5점
                () -> assertThat(rows.get(4).score()).isEqualTo(scoreGroup2),
                // 6위 (1개): 카테고리2, 생성일 4일 전 -> 26.5점
                () -> assertThat(rows.get(5).score()).isEqualTo(scoreGroup3),
                // 7위 (1개): 카테고리3, 생성일 60일 전 -> 1.0점
                () -> assertThat(rows.get(6).score()).isEqualTo(scoreGroup4)
        );
    }

    private List<ExploreScoreRow> findExploreScores(String userUuid) {
        return jdbcTemplate.query(
                """
                        SELECT hearit_id, score, cursor_id
                        FROM explore_score
                        WHERE user_uuid = ?
                        ORDER BY cursor_id
                        """,
                (rs, rowNum) -> new ExploreScoreRow(
                        rs.getLong("hearit_id"),
                        rs.getDouble("score"),
                        rs.getObject("cursor_id", Long.class)
                ),
                userUuid
        );
    }

    private double calculateRecencyScore(LocalDateTime createdAt, LocalDateTime now) {
        long daysPassed = Duration.between(createdAt.toLocalDate().atStartOfDay(), now.toLocalDate().atStartOfDay())
                .toDays();
        double recencyScore = 20.0 - (daysPassed * 0.5);
        return Math.max(0.0, recencyScore);
    }

    private record ExploreScoreRow(Long hearitId, double score, Long cursorId) {
    }
}
