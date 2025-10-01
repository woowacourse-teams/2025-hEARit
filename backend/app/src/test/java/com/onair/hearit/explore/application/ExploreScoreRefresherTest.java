package com.onair.hearit.explore.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;

import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
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

    @DisplayName("cursorId가 0이 아니면 갱신을 건너뛴다")
    @Test
    void skipRefreshingWhenCursorIsNotZero() {
        // given
        given(randomNumberGenerator.nextDouble()).willReturn(0.1d);
        String GUEST_UUID = UUID.randomUUID().toString();
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

    @DisplayName("cursorId가 0이면 점수를 갱신하고 커서를 부여한다")
    @Test
    void refreshScoresWhenCursorIsZero() {
        // given
        String GUEST_UUID = UUID.randomUUID().toString();
        double fixedRandom = 0.1d;
        double fixedRandomScore = fixedRandom * 10;
        given(randomNumberGenerator.nextDouble()).willReturn(fixedRandom);

        long cursorId = 0L;
        Category category1 = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit newest = dbHelper.insertHearitAt(TestFixture.createFixedHearitWith(category1), LocalDateTime.now());
        Hearit twoDaysAgo = dbHelper.insertHearitAt(TestFixture.createFixedHearitWith(category1),
                LocalDateTime.now().minusDays(2));
        Hearit oldest = dbHelper.insertHearitAt(TestFixture.createFixedHearitWith(category1),
                LocalDateTime.now().minusDays(25));

        // when
        exploreScoreRefresher.refreshIfNeeded(cursorId, GUEST_UUID, UserType.GUEST);

        // then
        List<ExploreScoreRow> rows = findExploreScores(GUEST_UUID);
        Map<Long, ExploreScoreRow> rowByHearitId = rows.stream()
                .collect(Collectors.toMap(ExploreScoreRow::hearitId, row -> row));

        LocalDateTime verificationTime = LocalDateTime.now();

        assertAll(
                // 1(랜덤 스코어) +  20 (최신 스코어)
                () -> assertThat(rowByHearitId.get(newest.getId()).score())
                        .isEqualTo(fixedRandomScore + calculateRecencyScore(newest.getCreatedAt(), verificationTime)),

                () -> assertThat(rowByHearitId.get(twoDaysAgo.getId()).score())
                        .isEqualTo(
                                fixedRandomScore + calculateRecencyScore(twoDaysAgo.getCreatedAt(), verificationTime)),
                () -> assertThat(rowByHearitId.get(oldest.getId()).score())
                        .isEqualTo(fixedRandomScore + calculateRecencyScore(oldest.getCreatedAt(), verificationTime))
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
