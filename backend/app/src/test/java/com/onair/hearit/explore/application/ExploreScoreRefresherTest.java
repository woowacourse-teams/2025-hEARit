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
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
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

    private static final String GUEST_UUID = UUID.randomUUID().toString();

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
        given(randomNumberGenerator.nextDouble()).willReturn(0.1d);
        long cursorId = 1L;

        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        for (int i = 0; i < 3; i++) {
            dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        }

        // when
        exploreScoreRefresher.refreshIfNeeded(cursorId, GUEST_UUID, UserType.GUEST);

        // then
        assertThat(findExploreScores(GUEST_UUID)).isEmpty();
    }

    @DisplayName("cursorId가 0이고 비회원이면 게스트 점수를 갱신하고 커서를 부여한다")
    @Test
    void refreshScoresWhenCursorIsZeroForGuest() {
        // given
        given(randomNumberGenerator.nextDouble()).willReturn(0.1d);
        long cursorId = 0L;
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        LocalDateTime now = LocalDateTime.now();

        // 게스트용 Hearit
        Hearit newest = dbHelper.insertHearitAt(TestFixture.createFixedHearitWith(category), now);
        Hearit twoDaysAgo = dbHelper.insertHearitAt(TestFixture.createFixedHearitWith(category), now.minusDays(2));
        Hearit fiveDaysAgo = dbHelper.insertHearitAt(TestFixture.createFixedHearitWith(category), now.minusDays(5));

        // when
        exploreScoreRefresher.refreshIfNeeded(cursorId, GUEST_UUID, UserType.GUEST);

        // then
        List<ExploreScoreRow> rows = findExploreScores(GUEST_UUID);
        Map<Long, ExploreScoreRow> rowByHearitId = rows.stream()
                .collect(Collectors.toMap(ExploreScoreRow::hearitId, row -> row));

        LocalDateTime verificationTime = LocalDateTime.now();
        double randomContribution = 0.1d * 10;

        assertAll(
                () -> assertThat(rows).hasSize(3),
                () -> assertThat(rowByHearitId.get(newest.getId()).score())
                        .isEqualTo(randomContribution + calculateRecencyScore(newest.getCreatedAt(),
                                verificationTime)),
                () -> assertThat(rowByHearitId.get(twoDaysAgo.getId()).score())
                        .isEqualTo(randomContribution + calculateRecencyScore(twoDaysAgo.getCreatedAt(),
                                verificationTime)),
                () -> assertThat(rowByHearitId.get(fiveDaysAgo.getId()).score())
                        .isEqualTo(randomContribution + calculateRecencyScore(fiveDaysAgo.getCreatedAt(),
                                verificationTime))

        );
    }

    @DisplayName("cursorId가 0이면 회원 점수를 갱신하면서 모든 ScoreFactor를 합산한다")
    @Test
    void refreshScoresWhenCursorIsZeroForMember() {
        // given
        given(randomNumberGenerator.nextDouble()).willReturn(0.1d);
        long cursorId = 0L;

        Category category1 = dbHelper.insertCategory(new Category("Java", "#112233"));
        Category category2 = dbHelper.insertCategory(new Category("Android", "#445566"));
        Category category3 = dbHelper.insertCategory(new Category("Kotlin", "#778899"));
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        LocalDateTime now = LocalDateTime.now();

        // -- 북마크 분포를 만드는 Hearit (카테고리1:3개, 카테고리2:1개) --
        Hearit bookmarked1 = dbHelper.insertHearitAt(TestFixture.createFixedHearitWith(category1), now);
        Hearit bookmarked2 = dbHelper.insertHearitAt(TestFixture.createFixedHearitWith(category1), now);
        Hearit bookmarked3 = dbHelper.insertHearitAt(TestFixture.createFixedHearitWith(category1), now);
        Hearit bookmarked4 = dbHelper.insertHearitAt(TestFixture.createFixedHearitWith(category2), now);
        dbHelper.insertBookmark(new Bookmark(member, bookmarked1));
        dbHelper.insertBookmark(new Bookmark(member, bookmarked2));
        dbHelper.insertBookmark(new Bookmark(member, bookmarked3));
        dbHelper.insertBookmark(new Bookmark(member, bookmarked4));

        // -- 실제 검증 대상 Hearit (recency + bookmark + random 합산) --
        Hearit withHighBookmark = dbHelper.insertHearitAt(TestFixture.createFixedHearitWith(category1), now);
        Hearit withLowBookmark = dbHelper.insertHearitAt(TestFixture.createFixedHearitWith(category2),
                now.minusDays(4));
        Hearit withNoBookmark = dbHelper.insertHearitAt(TestFixture.createFixedHearitWith(category3),
                now.minusDays(60));

        // when
        exploreScoreRefresher.refreshIfNeeded(cursorId, member.getUuid(), UserType.MEMBER);

        // then
        List<ExploreScoreRow> rows = findExploreScores(member.getUuid());
        Map<Long, ExploreScoreRow> rowByHearitId = rows.stream()
                .collect(Collectors.toMap(ExploreScoreRow::hearitId, row -> row));

        LocalDateTime verificationTime = LocalDateTime.now();
        double randomContribution = 0.1d * 10;
        double highBookmarkScore = (3.0 / 4.0) * 30.0;
        double lowBookmarkScore = (1.0 / 4.0) * 30.0;

        assertAll(
                () -> assertThat(rows).hasSize(7),
                () -> assertThat(rowByHearitId.get(withHighBookmark.getId()).score())
                        .isEqualTo(highBookmarkScore + calculateRecencyScore(withHighBookmark.getCreatedAt(),
                                verificationTime) + randomContribution),
                () -> assertThat(rowByHearitId.get(withLowBookmark.getId()).score())
                        .isEqualTo(lowBookmarkScore + calculateRecencyScore(withLowBookmark.getCreatedAt(),
                                verificationTime) + randomContribution),
                () -> assertThat(rowByHearitId.get(withNoBookmark.getId()).score())
                        .isEqualTo(0.0 + calculateRecencyScore(withNoBookmark.getCreatedAt(), verificationTime)
                                + randomContribution)
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
        long daysPassed = Duration.between(createdAt, now).toDays();
        double recencyScore = 20.0 - (daysPassed * 0.5);
        return Math.max(0.0, recencyScore);
    }

    private record ExploreScoreRow(Long hearitId, double score, Long cursorId) {
    }
}
