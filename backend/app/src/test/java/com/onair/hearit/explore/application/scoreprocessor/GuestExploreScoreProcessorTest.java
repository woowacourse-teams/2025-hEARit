package com.onair.hearit.explore.application.scoreprocessor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;

import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.ExploreScore;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.HearitKeyword;
import com.onair.hearit.domain.Keyword;
import com.onair.hearit.domain.UserInfo;
import com.onair.hearit.explore.application.ExploreScoreCalculator;
import com.onair.hearit.explore.application.ExploreScoreRefresher;
import com.onair.hearit.explore.application.scorefactor.BookmarkScoreFactor;
import com.onair.hearit.explore.application.scorefactor.RandomNumberGenerator;
import com.onair.hearit.explore.application.scorefactor.RandomScoreFactor;
import com.onair.hearit.explore.application.scorefactor.RecencyScoreFactor;
import com.onair.hearit.explore.dto.ExploredHearitResponse;
import com.onair.hearit.fixture.DbHelper;
import com.onair.hearit.infrastructure.jdbc.ExploreScoreCommandRepository;
import com.onair.hearit.infrastructure.jpa.ExploredHearitQueryRepository;
import com.onair.hearit.infrastructure.jpa.HearitKeywordRepository;
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
@Import({DbHelper.class, TestJpaAuditingConfig.class, RandomScoreFactor.class, RecencyScoreFactor.class,
        BookmarkScoreFactor.class, ExploreScoreCalculator.class, ExploreScoreCommandRepository.class,
        ExploreScoreRefresher.class})
@ActiveProfiles("integration-test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
class GuestExploreScoreProcessorTest {

    @MockitoBean
    private RandomNumberGenerator randomNumberGenerator;

    @Autowired
    private DbHelper dbHelper;

    @Autowired
    private ExploredHearitQueryRepository exploredHearitQueryRepository;

    @Autowired
    private HearitKeywordRepository hearitKeywordRepository;

    @Autowired
    private ExploreScoreRefresher exploreScoreRefresher;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private GuestExploreScoreProcessor guestExploreScoreProcessor;

    @BeforeEach
    void setup() {
        guestExploreScoreProcessor = new GuestExploreScoreProcessor(exploreScoreRefresher,
                exploredHearitQueryRepository,
                hearitKeywordRepository);
    }

    @DisplayName("게스트 사용자를 지원한다")
    @Test
    void isSupportedForGuest() {
        // given
        String guestUuid = UUID.randomUUID().toString();
        UserInfo guestInfo = new UserInfo(null, guestUuid);

        // when & then
        assertThat(guestExploreScoreProcessor.isSupported(guestInfo)).isTrue();
    }

    @DisplayName("게스트가 아니면 지원하지 않는다")
    @Test
    void isSupportedForNonGuest() {
        // given
        UserInfo memberInfo = new UserInfo(1L, null);

        // when
        // then
        assertAll(
                () -> assertThat(guestExploreScoreProcessor.isSupported(null)).isFalse(),
                () -> assertThat(guestExploreScoreProcessor.isSupported(memberInfo)).isFalse()
        );
    }

    @DisplayName("refreshScoresIfNeeded는 게스트 점수를 explore_score 테이블에 저장한다")
    @Test
    void refreshScoresIfNeededStoresScores() {
        // given
        long cursorId = 0L;
        String guestUuid = UUID.randomUUID().toString();
        UserInfo guestInfo = new UserInfo(null, guestUuid);

        // 랜덤 점수를 1.0점으로 고정
        double fixedRandomDouble = 0.1d;
        double fixedRandomScore = fixedRandomDouble * 10;
        given(randomNumberGenerator.getDouble()).willReturn(fixedRandomDouble);

        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        LocalDateTime now = LocalDateTime.now();
        dbHelper.insertHearitAt(TestFixture.createFixedHearitWith(category), now);
        dbHelper.insertHearitAt(TestFixture.createFixedHearitWith(category), now.minusDays(2));
        dbHelper.insertHearitAt(TestFixture.createFixedHearitWith(category), now.minusDays(5));

        // when
        guestExploreScoreProcessor.refreshScoresIfNeeded(guestInfo, cursorId);

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
                () -> assertThat(rows.get(0).score()).isEqualTo(scoreForNewest),      // 1위
                () -> assertThat(rows.get(1).score()).isEqualTo(scoreForTwoDaysAgo),  // 2위
                () -> assertThat(rows.get(2).score()).isEqualTo(scoreForFiveDaysAgo)   // 3위
        );
    }

    @DisplayName("fetchExploreHearits는 explore_score 테이블의 점수 순서대로 응답을 반환한다")
    @Test
    void getExploreHearitsReturnsResponses() {
        // given
        String guestUuid = UUID.randomUUID().toString();
        UserInfo guestInfo = new UserInfo(null, guestUuid);
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Keyword keyword = dbHelper.insertKeyword(TestFixture.createFixedKeyword());

        Hearit hearit1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Hearit hearit2 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        dbHelper.insertHearitKeyword(new HearitKeyword(hearit1, keyword)); // hearit1에만 키워드 설정

        dbHelper.insertExploreScore(new ExploreScore(guestUuid, hearit1.getId(), 50.0, 1L));
        dbHelper.insertExploreScore(new ExploreScore(guestUuid, hearit2.getId(), 40.0, 2L));

        // when
        List<ExploredHearitResponse> responses = guestExploreScoreProcessor.getExploreHearits(guestInfo, 0L, 3);

        // then
        assertThat(responses).hasSize(2);

        ExploredHearitResponse response1 = responses.get(0);
        ExploredHearitResponse response2 = responses.get(1);
        assertAll("첫 번째 응답 검증",
                () -> assertThat(response1.id()).isEqualTo(hearit1.getId()),
                () -> assertThat(response1.title()).isEqualTo(hearit1.getTitle()),
                () -> assertThat(response1.cursorId()).isEqualTo(1L),
                () -> assertThat(response1.isBookmarked()).isFalse(),
                () -> assertThat(response1.bookmarkId()).isNull(),
                () -> assertThat(response1.keywords()).hasSize(1),
                () -> assertThat(response2.id()).isEqualTo(hearit2.getId()),
                () -> assertThat(response2.title()).isEqualTo(hearit2.getTitle()),
                () -> assertThat(response2.cursorId()).isEqualTo(2L),
                () -> assertThat(response2.isBookmarked()).isFalse(),
                () -> assertThat(response2.bookmarkId()).isNull(),
                () -> assertThat(response2.keywords()).isEmpty() // hearit2는 키워드가 없음
        );
    }

    @DisplayName("점수 데이터가 없으면 비어 있는 리스트를 반환한다")
    @Test
    void getExploreHearitsReturnsEmptyWhenNoData() {
        // given
        String guestUuid = UUID.randomUUID().toString();
        UserInfo guestInfo = new UserInfo(null, guestUuid);

        // when
        List<ExploredHearitResponse> responses = guestExploreScoreProcessor.getExploreHearits(guestInfo, 0L, 3);

        // then
        assertThat(responses).isEmpty();
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
