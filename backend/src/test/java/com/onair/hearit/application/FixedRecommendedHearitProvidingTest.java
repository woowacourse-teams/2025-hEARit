package com.onair.hearit.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.onair.hearit.common.exception.custom.InvalidInputException;
import com.onair.hearit.config.TestJpaAuditingConfig;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.RecommendHearit;
import com.onair.hearit.fixture.DbHelper;
import com.onair.hearit.fixture.TestFixture;
import com.onair.hearit.infrastructure.HearitRepository;
import com.onair.hearit.infrastructure.RecommendHearitRepository;
import java.time.LocalDate;
import java.util.List;
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
class FixedRecommendedHearitProvidingTest {

    @Autowired
    private DbHelper dbHelper;

    @Autowired
    private HearitRepository hearitRepository;

    @Autowired
    private RecommendHearitRepository recommendHearitRepository;

    private FixedRecommendedHearitProviding recommendHearitProvider;

    @BeforeEach
    void setup() {
        recommendHearitProvider = new FixedRecommendedHearitProviding(hearitRepository, recommendHearitRepository);
    }

    @Test
    @DisplayName("최근 추천 히어릿을 요청 갯수만큼 제공한다.")
    void recentRecommendHearitTest() {
        // given
        int hearitCount = 5;
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        LocalDate today = LocalDate.now();
        for (int i = 0; i < hearitCount; i++) {
            Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
            dbHelper.insertRecommendHearit(new RecommendHearit(hearit, today));
        }

        // when
        List<Hearit> recommendHearits = recommendHearitProvider.getRecommendHearit(hearitCount);

        // then
        assertThat(recommendHearits).hasSize(hearitCount);
    }

    @Test
    @DisplayName("추천 히어릿이 요청 갯수보다 적으면 InvalidInputException을 던진다.")
    void recentRecommendHearitExceptionTest() {
        // given
        int hearitCount = 5;

        // when & then
        assertThatThrownBy(() -> recommendHearitProvider.getRecommendHearit(hearitCount))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("부족");
    }
}
