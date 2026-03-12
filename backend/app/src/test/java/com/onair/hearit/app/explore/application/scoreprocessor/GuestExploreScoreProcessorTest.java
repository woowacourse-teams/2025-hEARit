package com.onair.hearit.app.explore.application.scoreprocessor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.app.explore.application.ExploreScoreCalculator;
import com.onair.hearit.app.explore.application.ExploreScoreInitializer;
import com.onair.hearit.app.explore.application.ScoreFactorWeightConfig;
import com.onair.hearit.app.explore.application.scorefactor.BookmarkScoreFactor;
import com.onair.hearit.app.common.RandomNumberGenerator;
import com.onair.hearit.app.explore.application.scorefactor.RandomScoreFactor;
import com.onair.hearit.app.explore.application.scorefactor.RecencyScoreFactor;
import com.onair.hearit.app.explore.dto.ExploreCursor;
import com.onair.hearit.app.explore.dto.ExploredHearitResponse;
import com.onair.hearit.app.explore.dto.ExploredHearitResponseV3;
import com.onair.hearit.app.fixture.DbHelper;
import com.onair.hearit.core.config.DataSourceConfig;
import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.ExploreScore;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.UserInfo;
import com.onair.hearit.core.domain.UserType;
import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import com.onair.hearit.core.infrastructure.jdbc.ExploreScoreCommandRepository;
import com.onair.hearit.core.infrastructure.jpa.ExploredHearitQueryRepository;
import com.onair.hearit.core.infrastructure.jpa.HearitKeywordRepository;
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
@Import({DbHelper.class, TestJpaAuditingConfig.class, DataSourceConfig.class, RandomScoreFactor.class,
        RecencyScoreFactor.class, BookmarkScoreFactor.class, ExploreScoreCalculator.class,
        ExploreScoreCommandRepository.class, ExploreScoreInitializer.class, ScoreFactorWeightConfig.class,})
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
    private ExploreScoreInitializer exploreScoreInitializer;

    private GuestExploreScoreProcessor guestExploreScoreProcessor;

    @BeforeEach
    void setup() {
        guestExploreScoreProcessor = new GuestExploreScoreProcessor(exploreScoreInitializer,
                exploredHearitQueryRepository,
                hearitKeywordRepository);
    }

    @Test
    @DisplayName("게스트 사용자를 지원한다")
    void isSupportedForGuest() {
        UserInfo guestInfo = new UserInfo(UUID.randomUUID(), UserType.GUEST);
        assertThat(guestExploreScoreProcessor.isSupported(guestInfo)).isTrue();
    }

    @Test
    @DisplayName("게스트가 아니면 지원하지 않는다")
    void isSupportedForNonGuest() {
        UserInfo memberInfo = new UserInfo(UUID.randomUUID(), UserType.MEMBER);
        assertAll(
                () -> assertThat(guestExploreScoreProcessor.isSupported(null)).isFalse(),
                () -> assertThat(guestExploreScoreProcessor.isSupported(memberInfo)).isFalse()
        );
    }

    @Test
    @DisplayName("탐색 점수를 조회하면 DB의 score 데이터를 DTO로 변환하여 반환한다")
    void getExploreHearitsReturnsResponses() {
        // given
        UUID uuid = UUID.randomUUID();
        UserInfo guestInfo = new UserInfo(uuid, UserType.GUEST);
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Hearit hearit2 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        dbHelper.insertExploreScore(new ExploreScore(uuid, hearit1.getId(), 50.0, 1L));
        dbHelper.insertExploreScore(new ExploreScore(uuid, hearit2.getId(), 40.0, 2L));

        // when
        List<ExploredHearitResponse> responses = guestExploreScoreProcessor.getExploreHearits(guestInfo, 0L, 3);

        // then
        ExploredHearitResponse response1 = responses.get(0);
        ExploredHearitResponse response2 = responses.get(1);
        assertAll(
                () -> assertThat(responses).hasSize(2),
                () -> assertThat(response1.id()).isEqualTo(hearit1.getId()),
                () -> assertThat(response1.cursorId()).isEqualTo(1L),
                () -> assertThat(response2.id()).isEqualTo(hearit2.getId()),
                () -> assertThat(response2.cursorId()).isEqualTo(2L)
        );
    }

    @Test
    @DisplayName("v3: 탐색 점수를 조회하면 score 기반 커서가 포함된 V3 DTO로 반환한다")
    void getExploreHearitsReturnsResponsesV3() {
        // given
        UUID uuid = UUID.randomUUID();
        UserInfo guestInfo = new UserInfo(uuid, UserType.GUEST);
        Category category = dbHelper.insertCategory(new Category("V3Test", "#AABB00"));
        Hearit hearit1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Hearit hearit2 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        dbHelper.insertExploreScore(new ExploreScore(uuid, hearit1.getId(), 50.0, null));
        dbHelper.insertExploreScore(new ExploreScore(uuid, hearit2.getId(), 40.0, null));

        // when
        List<ExploredHearitResponseV3> responses = guestExploreScoreProcessor.getExploreHearits(guestInfo, ExploreCursor.initial(), 3);

        // then
        assertAll(
                () -> assertThat(responses).hasSize(2),
                () -> assertThat(responses.get(0).id()).isEqualTo(hearit1.getId()),
                () -> assertThat(responses.get(0).cursor()).isNotNull(),
                () -> assertThat(responses.get(1).id()).isEqualTo(hearit2.getId()),
                () -> assertThat(responses.get(1).cursor()).isNotNull()
        );
    }
}
