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
import com.onair.hearit.infrastructure.projection.ExploredHearitProjection;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@Sql("/dbclean.sql")
@Import({DbHelper.class, TestJpaAuditingConfig.class, RandomScoreFactor.class, RecencyScoreFactor.class,
        BookmarkScoreFactor.class, ExploreScoreCalculator.class, ExploreScoreCommandRepository.class,
        ExploreScoreRefresher.class})
@ActiveProfiles("integration-test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
class MemberExploreScoreProcessorTest {

    @MockBean
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

    private MemberExploreScoreProcessor memberExploreScoreProcessor;

    @BeforeEach
    void setup() {
        memberExploreScoreProcessor = new MemberExploreScoreProcessor(exploreScoreRefresher,
                exploredHearitQueryRepository,
                hearitKeywordRepository,
                memberRepository,
                bookmarkRepository);
    }

    @DisplayName("회원 사용자만 지원한다")
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

    @DisplayName("refreshScoresIfNeeded는 점수 데이터를 저장한다")
    @Test
    void refreshScoresIfNeededStoresScores() {
        // given
        given(randomNumberGenerator.nextDouble()).willReturn(0.1d);
        Member member = createMemberScenario();
        UserInfo memberInfo = new UserInfo(member.getId(), null);

        // when
        memberExploreScoreProcessor.refreshScoresIfNeeded(memberInfo, 0L);

        // then
        List<ExploredHearitProjection> projections = exploredHearitQueryRepository
                .findExploredHearits(member.getUuid(), 0L, Pageable.ofSize(10));
        assertThat(projections).isNotEmpty();
    }

    @DisplayName("fetchExploreHearits는 북마크와 키워드를 포함해 반환한다")
    @Test
    void fetchExploreHearitsReturnsResponses() {
        // given
        given(randomNumberGenerator.nextDouble()).willReturn(0.1d);
        Member member = createMemberScenario();
        UserInfo memberInfo = new UserInfo(member.getId(), null);
        memberExploreScoreProcessor.refreshScoresIfNeeded(memberInfo, 0L);

        // when
        List<ExploredHearitResponse> responses = memberExploreScoreProcessor.fetchExploreHearits(memberInfo, 0L, 3);

        // then
        assertAll(
                () -> assertThat(responses).hasSize(3),
                () -> assertThat(responses).anyMatch(ExploredHearitResponse::isBookmarked),
                () -> assertThat(responses.stream()
                        .filter(ExploredHearitResponse::isBookmarked)
                        .map(ExploredHearitResponse::bookmarkId)
                        .allMatch(id -> id != null && id > 0)).isTrue(),
                () -> assertThat(responses).allMatch(response -> !response.keywords().isEmpty())
        );
    }

    @DisplayName("탐색 데이터가 없으면 빈 리스트를 반환한다")
    @Test
    void fetchExploreHearitsReturnsEmptyWhenNoData() {
        // given
        given(randomNumberGenerator.nextDouble()).willReturn(0.1d);
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        UserInfo memberInfo = new UserInfo(member.getId(), null);

        // when
        List<ExploredHearitResponse> responses = memberExploreScoreProcessor.fetchExploreHearits(memberInfo, 0L, 3);

        // then
        assertThat(responses).isEmpty();
    }

    private Member createMemberScenario() {
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        Category category1 = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Category category2 = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Keyword keyword = dbHelper.insertKeyword(TestFixture.createFixedKeyword());

        Hearit hearit1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category1));
        Hearit hearit2 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category1));
        Hearit hearit3 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category1));
        Hearit hearit4 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category2));

        dbHelper.insertHearitKeyword(new HearitKeyword(hearit1, keyword));
        dbHelper.insertHearitKeyword(new HearitKeyword(hearit2, keyword));
        dbHelper.insertHearitKeyword(new HearitKeyword(hearit3, keyword));
        dbHelper.insertHearitKeyword(new HearitKeyword(hearit4, keyword));
        dbHelper.insertBookmark(new Bookmark(member, hearit1));
        dbHelper.insertBookmark(new Bookmark(member, hearit2));
        dbHelper.insertBookmark(new Bookmark(member, hearit3));
        dbHelper.insertBookmark(new Bookmark(member, hearit4));

        return member;
    }
}

