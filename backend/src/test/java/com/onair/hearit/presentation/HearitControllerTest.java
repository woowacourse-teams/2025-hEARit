package com.onair.hearit.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.TestFixture;
import com.onair.hearit.auth.infrastructure.jwt.JwtTokenProvider;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.HearitKeyword;
import com.onair.hearit.domain.Keyword;
import com.onair.hearit.domain.Member;
import com.onair.hearit.dto.response.HearitDetailResponse;
import com.onair.hearit.dto.response.HearitSearchResponse;
import com.onair.hearit.dto.response.RandomHearitResponse;
import com.onair.hearit.dto.response.RecommendHearitResponse;
import io.restassured.RestAssured;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

class HearitControllerTest extends IntegrationTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("로그인한 사용자가 히어릿 단일 조회 시, 200 OK 및 히어릿 정보를 제공한다.")
    void readHearitWithSuccessWithMember() {
        // given
        Member member = saveMember();
        String token = generateToken(member);
        Hearit hearit = saveHearitWithSuffix(1);

        // when & then
        HearitDetailResponse response = RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/v1/hearits/" + hearit.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract().as(HearitDetailResponse.class);

        assertThat(response.id()).isEqualTo(hearit.getId());
    }

    @Test
    @DisplayName("로그인 하지 않은 사용자가 히어릿 단일 조회 시, 200 OK 및 히어릿 정보를 제공한다.")
    void readHearitWithSuccessWithNotMember() {
        // given
        Hearit hearit = saveHearitWithSuffix(1);

        // when & then
        HearitDetailResponse response = RestAssured.given()
                .when()
                .get("/api/v1/hearits/" + hearit.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract().as(HearitDetailResponse.class);

        assertAll(
                () -> assertThat(response.id()).isEqualTo(hearit.getId()),
                () -> assertThat(response.isBookmarked()).isEqualTo(false)
        );
    }

    @Test
    @DisplayName("히어릿 단일 조회 시, 존재하지 않는 아이디인 경우 404 NOT_FOUND를 반환한다.")
    void readHearitWithNotFound() {
        // given
        Member member = saveMember();
        String token = generateToken(member);
        Long notFoundHearitId = 1L;

        // when & then
        RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .when()
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
        List<RandomHearitResponse> responses = RestAssured.given()
                .when()
                .get("/api/v1/hearits/random")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getList(".", RandomHearitResponse.class);

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
        List<RecommendHearitResponse> responses = RestAssured.given()
                .when()
                .get("/api/v1/hearits/recommend")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getList(".", RecommendHearitResponse.class);

        // then
        assertThat(responses).hasSize(3);
    }

    @Test
    @DisplayName("히어릿 검색 요청 시 200 OK 및 제목이 포함된 히어릿을 최신순으로 히어릿들을 반환한다.")
    void searchHearitsWithPaginationAndKeyword() {
        // given
        Keyword keyword = saveKeyword("Spring");
        Keyword keyword1 = saveKeyword("noKeyword");
        Hearit hearit = saveHearitWithTitleAndKeyword("examplespring1", keyword);// 검색어 포함됨
        Hearit hearit1 = saveHearitWithTitleAndKeyword("SPRING1example", keyword1);// 검색어 포함됨
        Hearit hearit2 = saveHearitWithTitleAndKeyword("notitle", keyword); // 검색어 포함됨
        Hearit hearit3 = saveHearitWithTitleAndKeyword("notitle", keyword1); // 검색어 포함 안 됨

        // when & then
        var response = RestAssured
                .given()
                .queryParam("searchTerm", "spring")
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
                () -> assertThat(response).extracting(HearitSearchResponse::id)
                        .containsExactlyInAnyOrder(hearit.getId(), hearit1.getId(), hearit2.getId())
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

    @Test
    @DisplayName("유효하지 않은 page 또는 size 값이 주어지면 400 BAD_REQUEST를 반환한다.")
    void searchHearitsByKeywordWithInvalidParams() {
        Keyword keyword = saveKeyword("DevOps");
        RestAssured.given()
                .queryParam("searchTerm", "abc")
                .queryParam("page", -1)
                .queryParam("size", 10)
                .when()
                .get("/api/v1/hearits/search")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());

        // size 음수
        RestAssured.given()
                .queryParam("keywordId", keyword.getId())
                .queryParam("page", 0)
                .queryParam("size", -5)
                .when()
                .get("/api/v1/hearits/search")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    private Member saveMember() {
        return dbHelper.insertMember(TestFixture.createFixedMember());
    }

    private String generateToken(Member member) {
        return jwtTokenProvider.createToken(member.getId());
    }

    private Hearit saveHearitWithSuffix(int suffix) {
        Category category = new Category("name" + suffix, "#123");
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
        Category category = new Category("category", "#123");
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

    private Category saveCategory(String name, String color) {
        Category category = new Category(name, color);
        return dbHelper.insertCategory(category);
    }

    private Keyword saveKeyword(String name) {
        return dbHelper.insertKeyword(new Keyword(name));
    }

    private Hearit saveHearitWithTitleAndKeyword(String title, Keyword keyword) {
        Category category = saveCategory("category", "#abc");
        Hearit hearit = new Hearit(title, "summary", 1, "originalAudioUrl", "shortAudioUrl", "scriptUrl", "source",
                LocalDateTime.now(), category);
        Hearit savedHearit = dbHelper.insertHearit(hearit);
        dbHelper.insertHearitKeyword(new HearitKeyword(savedHearit, keyword));
        return savedHearit;
    }
}
