package com.onair.hearit.core.infrastructure.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.RecommendHearit;
import com.onair.hearit.core.fixture.DbHelper;
import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import com.onair.hearit.infrastructure.jpa.RecommendHearitRepository;
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
                    dbHelper.insertRecommendHearit(new RecommendHearit(hearit, today));
                });
        LocalDate yesterday = today.minusDays(1);
        IntStream.rangeClosed(1, 5)
                .forEach((num) -> {
                    Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
                    dbHelper.insertRecommendHearit(new RecommendHearit(hearit, yesterday));
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

    @Test
    @DisplayName("시작일과 종료일 사이의 추천 히어릿을 Hearit 엔티티와 함께 조회한다")
    void findByRecommendDateIsBetweenWithHearit() {
        // given
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        LocalDate from = LocalDate.of(2025, 8, 10);
        LocalDate to = LocalDate.of(2025, 8, 20);
        LocalDate middleDate = from.plusDays(5);

        Hearit hearitIn1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        dbHelper.insertRecommendHearit(new RecommendHearit(hearitIn1, from)); // 시작일

        Hearit hearitIn2 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        dbHelper.insertRecommendHearit(new RecommendHearit(hearitIn2, to)); // 종료일

        Hearit hearitIn3 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        dbHelper.insertRecommendHearit(new RecommendHearit(hearitIn3, middleDate)); // 중간일

        Hearit hearitOut1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        dbHelper.insertRecommendHearit(new RecommendHearit(hearitOut1, from.minusDays(1)));
        Hearit hearitOut2 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        dbHelper.insertRecommendHearit(new RecommendHearit(hearitOut2, to.plusDays(1)));

        // when
        List<RecommendHearit> results = recommendHearitRepository.findByRecommendDateIsBetween(from, to);

        // then
        assertAll(
                () -> assertThat(results).hasSize(3),
                () -> assertThat(results)
                        .extracting(RecommendHearit::getHearit)
                        .containsExactlyInAnyOrder(hearitIn1, hearitIn2, hearitIn3),
                () -> assertThat(results)
                        .extracting(RecommendHearit::getRecommendDate)
                        .containsExactlyInAnyOrder(from, to, middleDate)
        );
    }
}
