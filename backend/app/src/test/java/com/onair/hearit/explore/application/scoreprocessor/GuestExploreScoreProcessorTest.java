package com.onair.hearit.explore.application.scoreprocessor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;

import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import com.onair.hearit.domain.Category;
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

    private static final String GUEST_ID = UUID.randomUUID().toString();

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

    private GuestExploreScoreProcessor guestExploreScoreProcessor;

    @BeforeEach
    void setup() {
        guestExploreScoreProcessor = new GuestExploreScoreProcessor(exploreScoreRefresher,
                exploredHearitQueryRepository,
                hearitKeywordRepository);
    }

    @DisplayName("게스트 사용자만 지원한다")
    @Test
    void isSupportedForGuest() {
        // given
        UserInfo guestInfo = new UserInfo(null, GUEST_ID);

        // when & then
        assertThat(guestExploreScoreProcessor.isSupported(guestInfo)).isTrue();
    }

    @DisplayName("게스트가 아니면 지원하지 않는다")
    @Test
    void isSupportedForNonGuest() {
        // given
        UserInfo memberInfo = new UserInfo(1L, null);

        // when & then
        assertAll(
                () -> assertThat(guestExploreScoreProcessor.isSupported(null)).isFalse(),
                () -> assertThat(guestExploreScoreProcessor.isSupported(memberInfo)).isFalse()
        );
    }

    @DisplayName("탐색 히어릿을 조회할 때 저장된 탐색 결과를 반환한다")
    @Test
    void fetchExploreHearitsReturnsResponses() {
        // given
        given(randomNumberGenerator.nextDouble()).willReturn(0.1d);
        UserInfo guestInfo = new UserInfo(null, GUEST_ID);
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Keyword keyword = dbHelper.insertKeyword(TestFixture.createFixedKeyword());

        Hearit hearit1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Hearit hearit2 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Hearit hearit3 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        dbHelper.insertHearitKeyword(new HearitKeyword(hearit1, keyword));
        dbHelper.insertHearitKeyword(new HearitKeyword(hearit2, keyword));
        dbHelper.insertHearitKeyword(new HearitKeyword(hearit3, keyword));
        guestExploreScoreProcessor.refreshScoresIfNeeded(guestInfo, 0L);

        // when
        List<ExploredHearitResponse> responses = guestExploreScoreProcessor.fetchExploreHearits(guestInfo, 0L, 3);

        // then
        assertAll(
                () -> assertThat(responses).hasSize(3),
                () -> assertThat(responses).allMatch(response -> !response.isBookmarked()),
                () -> assertThat(responses)
                        .allMatch(response -> response.cursorId() != null && response.cursorId() > 0),
                () -> assertThat(responses.getFirst().keywords()).isNotEmpty()
        );
    }

    @DisplayName("탐색 결과가 없으면 빈 리스트를 반환한다")
    @Test
    void fetchExploreHearitsReturnsEmptyWhenNoData() {
        // given
        given(randomNumberGenerator.nextDouble()).willReturn(0.1d);
        UserInfo guestInfo = new UserInfo(null, GUEST_ID);

        // when
        List<ExploredHearitResponse> responses = guestExploreScoreProcessor.fetchExploreHearits(guestInfo, 0L, 3);

        // then
        assertThat(responses).isEmpty();
    }
}
