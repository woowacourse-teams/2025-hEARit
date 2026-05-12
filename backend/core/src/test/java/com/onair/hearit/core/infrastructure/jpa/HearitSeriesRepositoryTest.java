package com.onair.hearit.core.infrastructure.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.Series;
import com.onair.hearit.core.fixture.DbHelper;
import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import({DbHelper.class, TestJpaAuditingConfig.class})
@ActiveProfiles("fake-test")
class HearitSeriesRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    DbHelper dbHelper;

    @Autowired
    HearitSeriesRepository hearitSeriesRepository;

    @Test
    @DisplayName("시리즈 ID로 히어릿을 createdAt 역순으로 조회한다.")
    void findHearitsBySeriesIdOrderByCreatedAtDesc_orderedByCreatedAtDesc() {
        // given
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Series series = dbHelper.insertSeries(TestFixture.createFixedSeries());

        LocalDateTime baseTime = LocalDateTime.of(2026, 4, 1, 0, 0);
        Hearit hearit1 = dbHelper.insertHearitAt(TestFixture.createFixedHearitWith(category), baseTime.minusDays(2));
        Hearit hearit2 = dbHelper.insertHearitAt(TestFixture.createFixedHearitWith(category), baseTime.minusDays(1));
        Hearit hearit3 = dbHelper.insertHearitAt(TestFixture.createFixedHearitWith(category), baseTime);
        dbHelper.assignHearitToSeries(hearit1, series);
        dbHelper.assignHearitToSeries(hearit2, series);
        dbHelper.assignHearitToSeries(hearit3, series);

        // when
        List<Hearit> result = hearitSeriesRepository.findHearitsBySeriesIdOrderByCreatedAtDesc(series.getId());

        // then
        assertAll(
                () -> assertThat(result).hasSize(3),
                () -> assertThat(result.get(0).getId()).isEqualTo(hearit3.getId()),
                () -> assertThat(result.get(1).getId()).isEqualTo(hearit2.getId()),
                () -> assertThat(result.get(2).getId()).isEqualTo(hearit1.getId())
        );
    }

    @Test
    @DisplayName("시리즈 ID로 조회 시 다른 시리즈의 히어릿은 포함되지 않는다.")
    void findHearitsBySeriesIdOrderByCreatedAtDesc_excludesOtherSeries() {
        // given
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Series series1 = dbHelper.insertSeries(TestFixture.createFixedSeries());
        Series series2 = dbHelper.insertSeries(TestFixture.createFixedSeries());

        Hearit hearit1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Hearit hearit2 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Hearit hearit3 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        dbHelper.assignHearitToSeries(hearit1, series1);
        dbHelper.assignHearitToSeries(hearit2, series1);
        dbHelper.assignHearitToSeries(hearit3, series2);

        // when
        List<Hearit> result = hearitSeriesRepository.findHearitsBySeriesIdOrderByCreatedAtDesc(series1.getId());

        // then
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result).extracting(Hearit::getId)
                        .containsExactlyInAnyOrder(hearit1.getId(), hearit2.getId())
        );
    }

    @Test
    @DisplayName("시리즈 ID로 조회 시 카테고리가 함께 로딩된다.")
    void findHearitsBySeriesIdOrderByCreatedAtDesc_fetchesCategory() {
        // given
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Series series = dbHelper.insertSeries(TestFixture.createFixedSeries());

        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        dbHelper.assignHearitToSeries(hearit, series);

        em.clear();

        // when
        List<Hearit> result = hearitSeriesRepository.findHearitsBySeriesIdOrderByCreatedAtDesc(series.getId());

        // then
        assertThat(result.get(0).getCategory().getId()).isEqualTo(category.getId());
    }

    @Test
    @DisplayName("동일한 (hearitId, seriesId) 쌍이 존재하면 true를 반환한다.")
    void existsByHearitIdAndSeriesId_returnsTrue() {
        // given
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Series series = dbHelper.insertSeries(TestFixture.createFixedSeries());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        dbHelper.assignHearitToSeries(hearit, series);

        // when & then
        assertThat(hearitSeriesRepository.existsByHearitIdAndSeriesId(hearit.getId(), series.getId())).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 쌍은 false를 반환한다.")
    void existsByHearitIdAndSeriesId_returnsFalse() {
        // when & then
        assertThat(hearitSeriesRepository.existsByHearitIdAndSeriesId(999L, 999L)).isFalse();
    }

    @Test
    @DisplayName("deleteByHearitIdAndSeriesId 호출 시 해당 연결만 삭제된다.")
    void deleteByHearitIdAndSeriesId_deletesOnlyTarget() {
        // given
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Series series1 = dbHelper.insertSeries(TestFixture.createFixedSeries());
        Series series2 = dbHelper.insertSeries(TestFixture.createFixedSeries());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        dbHelper.assignHearitToSeries(hearit, series1);
        dbHelper.assignHearitToSeries(hearit, series2);

        // when
        hearitSeriesRepository.deleteByHearitIdAndSeriesId(hearit.getId(), series1.getId());

        // then
        assertAll(
                () -> assertThat(hearitSeriesRepository.existsByHearitIdAndSeriesId(hearit.getId(), series1.getId())).isFalse(),
                () -> assertThat(hearitSeriesRepository.existsByHearitIdAndSeriesId(hearit.getId(), series2.getId())).isTrue()
        );
    }
}
