package com.onair.hearit.explore.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;

import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.UserType;
import com.onair.hearit.explore.application.scorefactor.BookmarkScoreFactor;
import com.onair.hearit.explore.application.scorefactor.RandomNumberGenerator;
import com.onair.hearit.explore.application.scorefactor.RandomScoreFactor;
import com.onair.hearit.explore.application.scorefactor.RecencyScoreFactor;
import com.onair.hearit.fixture.DbHelper;
import com.onair.hearit.infrastructure.jdbc.ExploreScoreCommandRepository;
import com.onair.hearit.infrastructure.jpa.ExploredHearitQueryRepository;
import com.onair.hearit.infrastructure.projection.ExploredHearitProjection;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
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
@Import({DbHelper.class, TestJpaAuditingConfig.class, ExploreScoreCommandRepository.class,
        RandomScoreFactor.class, RecencyScoreFactor.class, BookmarkScoreFactor.class, ExploreScoreCalculator.class})
@ActiveProfiles("integration-test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
class ExploreScoreRefresherTest {

    private static final String GUEST_UUID = UUID.randomUUID().toString();

    @MockBean
    private RandomNumberGenerator randomNumberGenerator;

    @Autowired
    private DbHelper dbHelper;

    @Autowired
    private ExploredHearitQueryRepository exploredHearitQueryRepository;

    @Autowired
    private ExploreScoreCommandRepository exploreScoreCommandRepository;

    @Autowired
    private ExploreScoreCalculator exploreScoreCalculator;

    private ExploreScoreRefresher exploreScoreRefresher;

    @BeforeEach
    void setUp() {
        exploreScoreRefresher = new ExploreScoreRefresher(exploreScoreCalculator, exploreScoreCommandRepository);
    }

    @DisplayName("cursorId가 0이 아니면 점수 갱신을 건너뛴다")
    @Test
    void skipRefreshingWhenCursorIsNotZero() {
        // given
        given(randomNumberGenerator.nextDouble()).willReturn(0.1d);
        long cursorId = 1L;

        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        TestFixture.createFixedHearitWith(category);
        for (int i = 0; i < 3; i++) {
            dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        }

        // when
        exploreScoreRefresher.refreshIfNeeded(cursorId, GUEST_UUID, UserType.GUEST);

        // then
        List<ExploredHearitProjection> projections = exploredHearitQueryRepository
                .findExploredHearits(GUEST_UUID, 0L, Pageable.ofSize(10));
        assertThat(projections).isEmpty();
    }

    @DisplayName("cursorId가 0이면 점수를 계산하고 커서 값을 갱신한다")
    @Test
    void refreshScoresWhenCursorIsZero() {
        // given
        given(randomNumberGenerator.nextDouble()).willReturn(0.1d);
        long cursorId = 0L;
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        TestFixture.createFixedHearitWith(category);
        for (int i = 0; i < 3; i++) {
            dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        }

        // when
        exploreScoreRefresher.refreshIfNeeded(cursorId, GUEST_UUID, UserType.GUEST);

        // then
        List<ExploredHearitProjection> projections = exploredHearitQueryRepository
                .findExploredHearits(GUEST_UUID, 0L, Pageable.ofSize(10));
        assertAll(
                () -> assertThat(projections).hasSize(3),
                () -> {
                    Assertions.assertNotNull(projections);
                    assertThat(projections.stream()
                            .map(ExploredHearitProjection::getCursorId))
                            .allMatch(id -> id != null && id > 0);
                }
        );
    }
}
