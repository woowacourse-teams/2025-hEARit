package com.onair.hearit.recommendhearit.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.RecommendHearit;
import com.onair.hearit.fixture.DbHelper;
import com.onair.hearit.infrastructure.jpa.RecommendHearitRepository;
import com.onair.hearit.recommendhearit.application.strategy.FixedRecommendedHearitStrategy;
import com.onair.hearit.recommendhearit.application.strategy.RecommendHearitStrategy;
import com.onair.hearit.recommendhearit.dto.RecommendHearitResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import({DbHelper.class, TestJpaAuditingConfig.class})
@ActiveProfiles("fake-test")
class RecommendHearitServiceTest {

    @Autowired
    private DbHelper dbHelper;

    @Autowired
    private RecommendHearitRepository recommendHearitRepository;

    private RecommendHearitService recommendHearitService;

    @BeforeEach
    void setup() {
        RecommendHearitStrategy recommendHearitStrategy = new FixedRecommendedHearitStrategy(recommendHearitRepository);
        recommendHearitService = new RecommendHearitService(recommendHearitStrategy);
    }

    @Test
    @DisplayName("최근 추천 히어릿 5개를 조회할 수 있다.")
    void getRecommendedHearits() {
        // given
        LocalDate today = LocalDate.now();
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        IntStream.rangeClosed(1, 3)
                .forEach(num -> {
                    Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
                    dbHelper.insertRecommendHearit(new RecommendHearit(hearit, today));
                });
        LocalDate yesterday = today.minusDays(1);
        IntStream.rangeClosed(1, 5)
                .forEach(num -> {
                    Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
                    dbHelper.insertRecommendHearit(new RecommendHearit(hearit, yesterday));
                });

        // when
        List<RecommendHearitResponse> hearits = recommendHearitService.getRecommendedHearits();

        // then
        assertThat(hearits).hasSize(5);
    }


}
