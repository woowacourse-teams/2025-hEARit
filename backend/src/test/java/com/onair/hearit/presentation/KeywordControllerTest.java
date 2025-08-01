package com.onair.hearit.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.domain.Keyword;
import com.onair.hearit.dto.response.KeywordResponse;
import com.onair.hearit.fixture.TestFixture;
import io.restassured.RestAssured;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class KeywordControllerTest extends IntegrationTest {

    @Test
    @DisplayName("전체 키워드를 조회 시 200 OK 및 페이징이 적용된 키워드 목록을 반환한다.")
    void readAllKeywords() {
        // given
        Keyword keyword1 = dbHelper.insertKeyword(TestFixture.createFixedKeyword());
        Keyword keyword2 = dbHelper.insertKeyword(TestFixture.createFixedKeyword());
        Keyword keyword3 = dbHelper.insertKeyword(TestFixture.createFixedKeyword());
        Keyword keyword4 = dbHelper.insertKeyword(TestFixture.createFixedKeyword());
        Keyword keyword5 = dbHelper.insertKeyword(TestFixture.createFixedKeyword());

        // when
        List<KeywordResponse> result = RestAssured.given()
                .param("page", 1)
                .param("size", 2)
                .when()
                .get("/api/v1/keywords")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getList(".", KeywordResponse.class);

        // then
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result).extracting(KeywordResponse::id)
                        .containsExactly(keyword3.getId(), keyword4.getId())
        );
    }


    @Test
    @DisplayName("단일 키워드 조회 시 200 OK 및 해당 키워드 정보를 반환한다.")
    void readSingleKeyword() {
        // given
        Keyword keyword = dbHelper.insertKeyword(TestFixture.createFixedKeyword());

        // when
        Keyword result = RestAssured.given()
                .when()
                .get("/api/v1/keywords/" + keyword.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(Keyword.class);

        // then
        assertAll(
                () -> assertThat(result.getId()).isEqualTo(keyword.getId()),
                () -> assertThat(result.getName()).isEqualTo(keyword.getName())
        );

    }

    @Test
    @DisplayName("존재하지 않는 키워드를 조회 시 404 NOT_FOUND를 반환한다.")
    void readNotFoundKeyword() {
        // when & then
        RestAssured.given()
                .when()
                .get("/api/v1/keywords/999")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("추천 키워드 조회 시 200 OK 및 요청 개수만큼의 키워드를 반환한다.")
    void readRecommendedKeywords() {
        // given
        dbHelper.insertKeyword(TestFixture.createFixedKeyword());
        dbHelper.insertKeyword(TestFixture.createFixedKeyword());
        dbHelper.insertKeyword(TestFixture.createFixedKeyword());
        int size = 2;

        // when
        List<Keyword> result = RestAssured.given()
                .param("size", size)
                .when()
                .get("/api/v1/keywords/recommend")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getList(".", Keyword.class);

        // then
        assertThat(result).hasSize(size);
    }
}
