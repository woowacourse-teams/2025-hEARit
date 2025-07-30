package com.onair.hearit.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.auth.infrastructure.jwt.JwtTokenProvider;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.HearitKeyword;
import com.onair.hearit.domain.Keyword;
import com.onair.hearit.domain.Member;
import com.onair.hearit.dto.response.HearitDetailResponse;
import com.onair.hearit.dto.response.HearitSearchResponse;
import com.onair.hearit.dto.response.HomeCategoryHearitResponse;
import com.onair.hearit.dto.response.PagedResponse;
import com.onair.hearit.dto.response.RandomHearitResponse;
import com.onair.hearit.dto.response.RecommendHearitResponse;
import com.onair.hearit.fixture.IntegrationTest;
import com.onair.hearit.fixture.TestFixture;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
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
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        String token = generateToken(member);
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

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
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

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
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
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
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        // when
        PagedResponse<RandomHearitResponse> responses = RestAssured.given()
                .when()
                .get("/api/v1/hearits/random")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(new TypeRef<>() {
                });

        // then
        assertThat(responses.content()).hasSize(3);
    }

    @Test
    @DisplayName("추천 히어릿을 조회 시, 200 OK 및 최대 5개 히어릿 정보 목록을 제공한다.")
    void readRecommendedHearits() {
        // given
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

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
    @DisplayName("히어릿 검색 요청 시 200 OK 및 제목 또는 키워드에 검색어가 포함된 히어릿을 최신순으로 반환한다.")
    void searchHearitsWithPagination() {
        // given
        Keyword keyword = dbHelper.insertKeyword(new Keyword("Spring"));
        Keyword keyword1 = dbHelper.insertKeyword(new Keyword("noKeyword"));

        Hearit hearit = saveHearitWithTitleAndKeyword("examplespring1", keyword);     // 제목 매칭
        Hearit hearit1 = saveHearitWithTitleAndKeyword("SPRING1example", keyword1);   // 제목 매칭
        Hearit hearit2 = saveHearitWithTitleAndKeyword("notitle", keyword);           // 키워드 매칭
        Hearit hearit3 = saveHearitWithTitleAndKeyword("notitle", keyword1);          // 매칭 안됨

        // when
        PagedResponse<HearitSearchResponse> pagedResponse = RestAssured
                .given()
                .queryParam("searchTerm", "spring")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/api/v1/hearits/search")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(new TypeRef<>() {
                });

        List<HearitSearchResponse> responses = pagedResponse.content();

        // then
        assertAll(
                () -> assertThat(responses).hasSize(3),
                () -> assertThat(responses).extracting(HearitSearchResponse::id)
                        .containsExactlyInAnyOrder(
                                hearit.getId(),
                                hearit1.getId(),
                                hearit2.getId())
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
    @DisplayName("홈 카테고리 히어릿 조회 시, 3개의 카테고리와 히어릿을 반환한다.")
    void readHomeCategoriesHearit() {
        // given
        Category category1 = dbHelper.insertCategory(TestFixture.createFixedCategory());
        dbHelper.insertHearit(TestFixture.createFixedHearitWith(category1));
        dbHelper.insertHearit(TestFixture.createFixedHearitWith(category1));
        dbHelper.insertHearit(TestFixture.createFixedHearitWith(category1));
        Category category2 = dbHelper.insertCategory(TestFixture.createFixedCategory());
        dbHelper.insertHearit(TestFixture.createFixedHearitWith(category2));
        dbHelper.insertHearit(TestFixture.createFixedHearitWith(category2));
        dbHelper.insertHearit(TestFixture.createFixedHearitWith(category2));
        Category category3 = dbHelper.insertCategory(TestFixture.createFixedCategory());
        dbHelper.insertHearit(TestFixture.createFixedHearitWith(category3));
        dbHelper.insertHearit(TestFixture.createFixedHearitWith(category3));
        dbHelper.insertHearit(TestFixture.createFixedHearitWith(category3));

        // when
        List<HomeCategoryHearitResponse> responses = RestAssured.given()
                .when().log().all()
                .get("/api/v1/hearits/home-categories")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getList(".", HomeCategoryHearitResponse.class);

        assertAll(
                () -> assertThat(responses.size()).isEqualTo(3),
                () -> assertThat(responses.get(0).hearits()).hasSize(3),
                () -> assertThat(responses.get(1).hearits()).hasSize(3),
                () -> assertThat(responses.get(2).hearits()).hasSize(3)
        );
    }

    private String generateToken(Member member) {
        return jwtTokenProvider.createToken(member.getId());
    }

    private Hearit saveHearitWithTitleAndKeyword(String title, Keyword keyword) {
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = new Hearit(
                title,
                "summary",
                100,
                "originalAudioUrl",
                "shortAudioUrl",
                "scriptUrl",
                "source",
                category);
        Hearit savedHearit = dbHelper.insertHearit(hearit);
        dbHelper.insertHearitKeyword(new HearitKeyword(savedHearit, keyword));
        return savedHearit;
    }
}
