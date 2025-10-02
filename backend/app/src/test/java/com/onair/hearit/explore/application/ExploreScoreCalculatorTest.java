package com.onair.hearit.explore.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;

import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.UserType;
import com.onair.hearit.explore.application.scorefactor.ScoreFactor;
import com.onair.hearit.fixture.DbHelper;
import com.onair.hearit.infrastructure.jpa.HearitRepository;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@Sql("/dbclean.sql")
// 1. Mockito 사용을 위해 @ExtendWith(MockitoExtension.class) 추가
@ExtendWith(MockitoExtension.class)
@Import({DbHelper.class, TestJpaAuditingConfig.class})
@ActiveProfiles("integration-test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
class ExploreScoreCalculatorTest {

    @Mock
    private ScoreFactor scoreFactor1;

    @Mock
    private ScoreFactor scoreFactor2;

    @Mock
    private ScoreFactor scoreFactor3;

    @Autowired
    private DbHelper dbHelper;

    @Autowired
    private HearitRepository hearitRepository;

    private ExploreScoreCalculator exploreScoreCalculator;

    @BeforeEach
    void setUp() {
        exploreScoreCalculator = new ExploreScoreCalculator(hearitRepository,
                List.of(scoreFactor1, scoreFactor2, scoreFactor3));
    }

    @DisplayName("지원되는 모든 ScoreFactor 들의 점수를 합산하여 Map 으로 반환한다")
    @Test
    void calculateTotalScores_sumsScoresFromMockedFactors() {
        // given
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Hearit hearit2 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        given(scoreFactor1.isSupported(any())).willReturn(true);
        given(scoreFactor1.calculate(any(), anyList())).willReturn(Map.of(hearit1.getId(), 1.0, hearit2.getId(), 1.0));

        given(scoreFactor2.isSupported(any())).willReturn(true);
        given(scoreFactor2.calculate(any(), anyList())).willReturn(
                Map.of(hearit1.getId(), 20.0, hearit2.getId(), 18.0));

        given(scoreFactor3.isSupported(any())).willReturn(false);
        // scoreFactor3.calculate()는 호출되지 않으므로, given 설정이 불필요함 (에러 발생 X)

        Map<Long, Double> memberScores = exploreScoreCalculator.calculateTotalScores("any-uuid", UserType.MEMBER);

        // then
        assertAll(
                () -> assertThat(memberScores.get(hearit1.getId())).isEqualTo(1.0 + 20.0), // 21
                () -> assertThat(memberScores.get(hearit2.getId())).isEqualTo(1.0 + 18.0)  // 19
        );
    }

}
