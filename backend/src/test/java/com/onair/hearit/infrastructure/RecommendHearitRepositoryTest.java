package com.onair.hearit.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.config.TestJpaAuditingConfig;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.RecommendHearit;
import com.onair.hearit.fixture.DbHelper;
import com.onair.hearit.fixture.TestFixture;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("fake-test")
@Import({DbHelper.class, TestJpaAuditingConfig.class})
class RecommendHearitRepositoryTest {

    @Autowired
    private RecommendHearitRepository recommendHearitRepository;

    @Autowired
    private DbHelper dbHelper;

    @Test
    @DisplayName("당일 제외 가장 최근 추천 히어릿 N개를 조회한다")
    void findByRecommendDateLimitN() {
        // given
        LocalDate today = LocalDate.now();
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        IntStream.rangeClosed(1, 3)
                .forEach((num) -> {
                    Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
                    dbHelper.insertRecommendHearit(new RecommendHearit(hearit.getId(), today));
                });
        LocalDate yesterday = today.minusDays(1);
        IntStream.rangeClosed(1, 5)
                .forEach((num) -> {
                    Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
                    dbHelper.insertRecommendHearit(new RecommendHearit(hearit.getId(), yesterday));
                });

        // when
        List<RecommendHearit> recommendHearits = recommendHearitRepository.findByRecentRecommendDateLimitN(
                today, 5);

        // then
        assertAll(() -> {
            assertThat(recommendHearits).hasSize(5);
            assertThat(recommendHearits.getFirst().getRecommendDate()).isEqualTo(today);
            assertThat(recommendHearits.getLast().getRecommendDate()).isEqualTo(yesterday);
        });
    }
}