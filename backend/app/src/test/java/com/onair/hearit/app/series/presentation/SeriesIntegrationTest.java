package com.onair.hearit.app.series.presentation;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.app.common.dto.response.PagedResponse;
import com.onair.hearit.app.fixture.IntegrationTest;
import com.onair.hearit.app.series.dto.SeriesDetailResponse;
import com.onair.hearit.app.series.dto.SeriesOverviewResponse;
import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.Series;
import com.onair.hearit.core.fixture.TestFixture;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class SeriesIntegrationTest extends IntegrationTest {

    @Test
    @DisplayName("시리즈 목록 조회 시 200 OK 및 페이지네이션이 적용된 시리즈 목록을 반환한다.")
    void readSeriesList() {
        // given
        for (int i = 0; i < 5; i++) {
            dbHelper.insertSeries(TestFixture.createFixedSeries());
        }

        // when
        PagedResponse<SeriesOverviewResponse> result = RestAssured.given(this.spec)
                .param("page", 1)
                .param("size", 2)
                .when()
                .get("/api/v1/series")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(new TypeRef<>() {});

        // then
        assertAll(
                () -> assertThat(result.content()).hasSize(2),
                () -> assertThat(result.totalElements()).isEqualTo(5),
                () -> assertThat(result.totalPages()).isEqualTo(3)
        );
    }

    @Test
    @DisplayName("시리즈 목록 조회 시 유효하지 않은 페이지 번호를 보내면 400 BAD_REQUEST를 반환한다.")
    void readSeriesListWithInvalidPage() {
        RestAssured.given(this.spec)
                .param("page", -1)
                .param("size", 10)
                .when()
                .get("/api/v1/series")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("시리즈 상세 조회 시 200 OK 및 에피소드 목록을 포함한 시리즈 정보를 반환한다.")
    void readSeriesDetail() {
        // given
        Series series = dbHelper.insertSeries(TestFixture.createFixedSeries());
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());

        Hearit hearit1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        dbHelper.assignHearitToSeries(hearit1, series);

        Hearit hearit2 = dbHelper.insertHearit(TestFixture.createHearitWith("에피소드2", category));
        dbHelper.assignHearitToSeries(hearit2, series);

        // when
        SeriesDetailResponse response = RestAssured.given(this.spec)
                .when()
                .get("/api/v1/series/{seriesId}", series.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(SeriesDetailResponse.class);

        // then
        assertAll(
                () -> assertThat(response.id()).isEqualTo(series.getId()),
                () -> assertThat(response.title()).isEqualTo(series.getTitle()),
                () -> assertThat(response.hearits()).hasSize(2)
        );
    }

    @Test
    @DisplayName("존재하지 않는 시리즈 조회 시 404 NOT_FOUND를 반환한다.")
    void readSeriesDetail_notFound() {
        RestAssured.given(this.spec)
                .when()
                .get("/api/v1/series/{seriesId}", 99999L)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }
}
