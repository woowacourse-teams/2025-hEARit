package com.onair.hearit.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.dto.response.HearitDetailResponse;
import com.onair.hearit.dto.response.HearitSearchResponse;
import io.restassured.RestAssured;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class HearitControllerTest extends IntegrationTest {

    @Test
    @DisplayName("히어릿 단일 조회 시, 200 OK 및 히어릿 정보를 제공한다.")
    void readHearitWithSuccess() {
        // given
        Hearit hearit = saveHearitWithSuffix(1);

        // when & then
        HearitDetailResponse response = RestAssured.when()
                .get("/api/v1/hearits/" + hearit.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract().as(HearitDetailResponse.class);

        assertThat(response.id()).isEqualTo(hearit.getId());
    }

    @Test
    @DisplayName("히어릿 단일 조회 시, 존재하지 않는 아이디인 경우 404 NOT_FOUND를 반환한다.")
    void readHearitWithNotFound() {
        // given
        Long notFoundHearitId = 1L;

        // when & then
        RestAssured.when()
                .get("/api/v1/hearits/" + notFoundHearitId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("랜덤 히어릿을 조회 시, 200 OK 및 최대 10개 히어릿 정보 목록을 제공한다.")
    void readRandomHearits() {
        // given
        saveHearitWithSuffix(1);
        saveHearitWithSuffix(2);
        saveHearitWithSuffix(3);

        // when
        List<HearitDetailResponse> responses = RestAssured.given()
                .when()
                .get("/api/v1/hearits/random")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getList(".", HearitDetailResponse.class);

        // then
        assertThat(responses).hasSize(3);
    }

    @Test
    @DisplayName("추천 히어릿을 조회 시, 200 OK 및 최대 5개 히어릿 정보 목록을 제공한다.")
    void readRecommendedHearits() {
        // given
        saveHearitWithSuffix(1);
        saveHearitWithSuffix(2);
        saveHearitWithSuffix(3);

        // when
        List<HearitDetailResponse> responses = RestAssured.given()
                .when()
                .get("/api/v1/hearits/recommend")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getList(".", HearitDetailResponse.class);

        // then
        assertThat(responses).hasSize(3);
    }


    @Test
    @DisplayName("히어릿 검색 요청 시 200 OK 및 제목이 포함된 히어릿을 최신순으로 히어릿들을 반환한다.")
    void searchHearitsWithPaginationAndKeyword() {
        // given
        Hearit hearit1 = saveHearitByTitle("exampletitle1");
        Hearit hearit2 = saveHearitByTitle("title1example");
        Hearit hearit3 = saveHearitByTitle("wwtitle1ww");
        Hearit hearit4 = saveHearitByTitle("notitle");

        // when & then
        var response = RestAssured
                .given()
                .queryParam("searchTerm", "title1")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/api/v1/hearits/search")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract().jsonPath().getList(".", HearitSearchResponse.class);

        // then
        assertAll(
                () -> assertThat(response).hasSize(3),
                () -> assertThat(response).extracting(HearitSearchResponse::title)
                        .allSatisfy(title -> assertThat(title).contains("title1"))
        );
    }

    @Test
    @DisplayName("검색 파라미터가 유효하지 않을 때 400 에러를 반환한다. ")
    void searchHearitsWithInvalidParams() {
        // when & then
        RestAssured.given()
                .queryParam("searchTerm", "title1")
                .queryParam("page", -1)
                .queryParam("size", 10)
                .when()
                .get("/api/v1/hearits/search")
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value());

        RestAssured.given()
                .queryParam("searchTerm", "title1")
                .queryParam("page", 0)
                .queryParam("size", -5)
                .when()
                .get("/api/v1/hearits/search")
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    private Hearit saveHearitWithSuffix(int suffix) {
        Category category = new Category("name" + suffix);
        dbHelper.insertCategory(category);

        Hearit hearit = new Hearit(
                "title" + suffix,
                "summary" + suffix, suffix,
                "originalAudioUrl" + suffix,
                "shortAudioUrl" + suffix,
                "scriptUrl" + suffix,
                "source" + suffix,
                LocalDateTime.now(),
                category);
        return dbHelper.insertHearit(hearit);
    }

    private Hearit saveHearitByTitle(String title) {
        Category category = new Category("category");
        dbHelper.insertCategory(category);
        Hearit hearit = new Hearit(
                title,
                "summary",
                1,
                "originalAudioUrl",
                "shortAudioUrl",
                "scriptUrl",
                "source",
                LocalDateTime.now(),
                category);
        return dbHelper.insertHearit(hearit);
    }
}
