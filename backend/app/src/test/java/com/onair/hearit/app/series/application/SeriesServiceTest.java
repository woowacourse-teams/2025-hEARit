package com.onair.hearit.app.series.application;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.onair.hearit.app.common.dto.request.PagingRequest;
import com.onair.hearit.app.common.dto.response.PagedResponse;
import com.onair.hearit.app.exception.custom.NotFoundException;
import com.onair.hearit.app.fixture.DbHelper;
import com.onair.hearit.app.series.dto.SeriesDetailResponse;
import com.onair.hearit.app.series.dto.SeriesOverviewResponse;
import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.Series;
import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import({DbHelper.class, TestJpaAuditingConfig.class})
@ActiveProfiles("fake-test")
class SeriesServiceTest {

    @Autowired
    private DbHelper dbHelper;

    private SeriesService seriesService;

    @Autowired
    private com.onair.hearit.core.infrastructure.jpa.SeriesRepository seriesRepository;

    @Autowired
    private com.onair.hearit.core.infrastructure.jpa.HearitRepository hearitRepository;

    @BeforeEach
    void setUp() {
        seriesService = new SeriesService(seriesRepository, hearitRepository);
    }

    @Nested
    @DisplayName("시리즈 목록 조회")
    class GetSeriesTest {

        @Test
        @DisplayName("시리즈 목록 조회 시 페이지네이션이 적용되어 반환된다.")
        void getSeries_withPagination() {
            // given
            for (int i = 0; i < 5; i++) {
                dbHelper.insertSeries(TestFixture.createFixedSeries());
            }
            PagingRequest pagingRequest = new PagingRequest(1, 2);

            // when
            PagedResponse<SeriesOverviewResponse> result = seriesService.getSeries(pagingRequest);

            // then
            assertAll(
                    () -> assertThat(result.content()).hasSize(2),
                    () -> assertThat(result.totalElements()).isEqualTo(5),
                    () -> assertThat(result.totalPages()).isEqualTo(3)
            );
        }

        @Test
        @DisplayName("시리즈가 없을 때 빈 목록을 반환한다.")
        void getSeries_empty() {
            // when
            PagedResponse<SeriesOverviewResponse> result = seriesService.getSeries(new PagingRequest(0, 20));

            // then
            assertAll(
                    () -> assertThat(result.content()).isEmpty(),
                    () -> assertThat(result.totalElements()).isZero()
            );
        }

        @Test
        @DisplayName("시리즈 목록은 id 내림차순으로 정렬된다.")
        void getSeries_orderedByIdDesc() {
            // given
            Series series1 = dbHelper.insertSeries(TestFixture.createFixedSeries());
            Series series2 = dbHelper.insertSeries(TestFixture.createFixedSeries());
            Series series3 = dbHelper.insertSeries(TestFixture.createFixedSeries());

            // when
            PagedResponse<SeriesOverviewResponse> result = seriesService.getSeries(new PagingRequest(0, 20));

            // then
            assertThat(result.content())
                    .extracting(SeriesOverviewResponse::id)
                    .containsExactly(series3.getId(), series2.getId(), series1.getId());
        }
    }

    @Nested
    @DisplayName("시리즈 상세 조회")
    class GetSeriesDetailTest {

        @Test
        @DisplayName("시리즈 상세 조회 시 시리즈 정보와 에피소드 목록을 반환한다.")
        void getSeriesDetail_success() {
            // given
            Series series = dbHelper.insertSeries(TestFixture.createFixedSeries());
            Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());

            Hearit hearit1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
            dbHelper.assignHearitToSeries(hearit1, series);

            Hearit hearit2 = dbHelper.insertHearit(TestFixture.createHearitWith("에피소드2", category));
            dbHelper.assignHearitToSeries(hearit2, series);

            // when
            SeriesDetailResponse response = seriesService.getSeriesDetail(series.getId());

            // then
            assertAll(
                    () -> assertThat(response.id()).isEqualTo(series.getId()),
                    () -> assertThat(response.title()).isEqualTo(series.getTitle()),
                    () -> assertThat(response.hearits()).hasSize(2)
            );
        }

        @Test
        @DisplayName("존재하지 않는 seriesId로 조회 시 NotFoundException이 발생한다.")
        void getSeriesDetail_notFound() {
            assertThrows(NotFoundException.class,
                    () -> seriesService.getSeriesDetail(99999L));
        }

        @Test
        @DisplayName("시리즈에 에피소드가 없을 때 빈 hearits 목록을 반환한다.")
        void getSeriesDetail_noHearits() {
            // given
            Series series = dbHelper.insertSeries(TestFixture.createFixedSeries());

            // when
            SeriesDetailResponse response = seriesService.getSeriesDetail(series.getId());

            // then
            assertThat(response.hearits()).isEmpty();
        }
    }
}
