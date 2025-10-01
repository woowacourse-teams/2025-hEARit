package com.onair.hearit.explore.application.scoreprocessor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;

import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import com.onair.hearit.domain.Bookmark;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.HearitKeyword;
import com.onair.hearit.domain.Keyword;
import com.onair.hearit.domain.Member;
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
import com.onair.hearit.infrastructure.jpa.BookmarkRepository;
import com.onair.hearit.infrastructure.jpa.ExploredHearitQueryRepository;
import com.onair.hearit.infrastructure.jpa.HearitKeywordRepository;
import com.onair.hearit.infrastructure.jpa.MemberRepository;
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
class MemberExploreScoreProcessorTest {

    @MockitoBean
    private RandomNumberGenerator randomNumberGenerator;

    @Autowired
    private DbHelper dbHelper;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ExploredHearitQueryRepository exploredHearitQueryRepository;

    @Autowired
    private HearitKeywordRepository hearitKeywordRepository;

    @Autowired
    private ExploreScoreRefresher exploreScoreRefresher;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private MemberExploreScoreProcessor memberExploreScoreProcessor;

    @BeforeEach
    void setup() {
        memberExploreScoreProcessor = new MemberExploreScoreProcessor(exploreScoreRefresher,
                exploredHearitQueryRepository,
                hearitKeywordRepository,
                memberRepository,
                bookmarkRepository);
    }

    @DisplayName("회원 사용자를 지원한다")
    @Test
    void isSupportedForMember() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        UserInfo memberInfo = new UserInfo(member.getId(), null);

        // when & then
        assertThat(memberExploreScoreProcessor.isSupported(memberInfo)).isTrue();
    }

    @DisplayName("회원이 아니면 지원하지 않는다")
    @Test
    void isSupportedForNonMember() {
        // given
        UserInfo guestInfo = new UserInfo(null, UUID.randomUUID().toString());
        UserInfo nonExistingMember = new UserInfo(999L, null);

        // when & then
        assertAll(
                () -> assertThat(memberExploreScoreProcessor.isSupported(null)).isFalse(),
                () -> assertThat(memberExploreScoreProcessor.isSupported(guestInfo)).isFalse(),
                () -> assertThat(memberExploreScoreProcessor.isSupported(nonExistingMember)).isFalse()
        );
    }

    @DisplayName("refreshScoresIfNeeded는 회원 점수를 explore_score 테이블에 저장한다")
    @Test
    void refreshScoresIfNeededStoresScores() {
        // given
        long cursorId = 0L;

        // 랜덤 점수를 1.0점으로 고정
        double fixedRandomDouble = 0.1d;
        double fixedRandomScore = fixedRandomDouble * 10;
        given(randomNumberGenerator.getDouble()).willReturn(fixedRandomDouble);

        Category category1 = dbHelper.insertCategory(new Category("Java", "#112233"));
        Category category2 = dbHelper.insertCategory(new Category("Android", "#445566"));
        Category category3 = dbHelper.insertCategory(new Category("Kotlin", "#778899"));
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        UserInfo memberInfo = new UserInfo(member.getId(), null);
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
        memberExploreScoreProcessor.refreshScoresIfNeeded(memberInfo, cursorId);

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
                () -> assertThat(rows.get(0).score()).isEqualTo(scoreGroup1), // 1~4위: 43.5점
                () -> assertThat(rows.get(1).score()).isEqualTo(scoreGroup1),
                () -> assertThat(rows.get(2).score()).isEqualTo(scoreGroup1),
                () -> assertThat(rows.get(3).score()).isEqualTo(scoreGroup1),
                () -> assertThat(rows.get(4).score()).isEqualTo(scoreGroup2), // 5위: 28.5점
                () -> assertThat(rows.get(5).score()).isEqualTo(scoreGroup3), // 6위: 26.5점
                () -> assertThat(rows.get(6).score()).isEqualTo(scoreGroup4)  // 7위: 1.0점
        );
    }

    @DisplayName("fetchExploreHearits는 점수판 순서대로 응답과 북마크 정보를 함께 반환한다")
    @Test
    void getExploreHearitsReturnsResponses() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        UserInfo memberInfo = new UserInfo(member.getId(), null);
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Keyword keyword = dbHelper.insertKeyword(TestFixture.createFixedKeyword());

        // 테스트 데이터 생성
        Hearit hearit1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category)); // 북마크 될 Hearit
        Hearit hearit2 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        dbHelper.insertHearitKeyword(new HearitKeyword(hearit1, keyword));
        dbHelper.insertHearitKeyword(new HearitKeyword(hearit2, keyword));
        Bookmark bookmark = dbHelper.insertBookmark(new Bookmark(member, hearit1)); // hearit1만 북마크

        jdbcTemplate.update("""
                INSERT INTO explore_score (user_uuid, hearit_id, score, cursor_id)
                VALUES (?, ?, ?, ?)
                """, member.getUuid(), hearit1.getId(), 70.0, 1L);
        jdbcTemplate.update("""
                INSERT INTO explore_score (user_uuid, hearit_id, score, cursor_id)
                VALUES (?, ?, ?, ?)
                """, member.getUuid(), hearit2.getId(), 50.0, 2L);

        // when
        List<ExploredHearitResponse> responses = memberExploreScoreProcessor.getExploreHearits(memberInfo, 0L, 3);

        // then

        ExploredHearitResponse response1 = responses.get(0);
        ExploredHearitResponse response2 = responses.get(1);
        assertAll(
                () -> assertThat(responses).hasSize(2),
                () -> assertThat(response1.id()).isEqualTo(hearit1.getId()),
                () -> assertThat(response1.cursorId()).isEqualTo(1L),
                () -> assertThat(response1.isBookmarked()).isTrue(),
                () -> assertThat(response1.bookmarkId()).isEqualTo(bookmark.getId()),
                () -> assertThat(response1.keywords()).isNotEmpty(),
                () -> assertThat(response2.id()).isEqualTo(hearit2.getId()),
                () -> assertThat(response2.cursorId()).isEqualTo(2L),
                () -> assertThat(response2.isBookmarked()).isFalse(),
                () -> assertThat(response2.bookmarkId()).isNull(),
                () -> assertThat(response2.keywords()).isNotEmpty()
        );
    }

    @DisplayName("점수 데이터가 없으면 비어 있는 리스트를 반환한다")
    @Test
    void getExploreHearitsReturnsEmptyWhenNoData() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        UserInfo memberInfo = new UserInfo(member.getId(), null);

        // when
        List<ExploredHearitResponse> responses = memberExploreScoreProcessor.getExploreHearits(memberInfo, 0L, 3);

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
