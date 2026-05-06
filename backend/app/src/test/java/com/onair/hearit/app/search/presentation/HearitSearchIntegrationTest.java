package com.onair.hearit.app.search.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;

import com.onair.hearit.app.auth.infrastructure.jwt.JwtTokenProvider;
import com.onair.hearit.app.common.dto.response.PagedResponse;
import com.onair.hearit.app.fixture.IntegrationTest;
import com.onair.hearit.app.search.dto.HearitSearchResponse;
import com.onair.hearit.app.search.dto.SearchAutocompleteResponse;
import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.HearitKeyword;
import com.onair.hearit.core.domain.Keyword;
import com.onair.hearit.core.domain.Member;
import com.onair.hearit.core.domain.Source;
import com.onair.hearit.core.fixture.TestFixture;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;

public class HearitSearchIntegrationTest extends IntegrationTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("v1 히어릿 검색 요청 시 200 OK 및 제목 또는 키워드에 검색어가 포함된 히어릿을 최신순으로 반환한다.")
    void readHearitsByCategoryWithPagination() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        String token = generateToken(member);
        Keyword keyword = dbHelper.insertKeyword(new Keyword("Spring"));
        Keyword keyword1 = dbHelper.insertKeyword(new Keyword("noKeyword"));

        Hearit hearit = saveHearitWithTitleAndKeyword("examplespring1", keyword);
        Hearit hearit1 = saveHearitWithTitleAndKeyword("SPRING1example", keyword1);
        Hearit hearit2 = saveHearitWithTitleAndKeyword("notitle", keyword);
        saveHearitWithTitleAndKeyword("notitle", keyword1);

        // when
        PagedResponse<HearitSearchResponse> pagedResponse = RestAssured.given(this.spec)
                .header("Authorization", "Bearer " + token)
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
    @DisplayName("v1 검색 파라미터가 유효하지 않을 때 400 에러를 반환한다. ")
    void readHearitsByCategoryWithInvalidParams() {
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        String token = generateToken(member);
        // when & then
        RestAssured.given(this.spec)
                .header("Authorization", "Bearer " + token)
                .queryParam("searchTerm", "spring")
                .queryParam("page", -1)
                .queryParam("size", 10)
                .when()
                .get("/api/v1/hearits/search")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());

        RestAssured.given(this.spec)
                .header("Authorization", "Bearer " + token)
                .queryParam("searchTerm", "spring")
                .queryParam("page", 0)
                .queryParam("size", -1)
                .when()
                .get("/api/v1/hearits/search")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("v2 히어릿 검색 요청 시 200 OK 및 제목 또는 키워드에 검색어가 포함된 히어릿을 반환한다.")
    void readHearitsByCategoryWithPaginationV2() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        String token = generateToken(member);
        Keyword keyword = dbHelper.insertKeyword(new Keyword("Spring"));
        Keyword keyword1 = dbHelper.insertKeyword(new Keyword("noKeyword"));

        Hearit hearit = saveHearitWithTitleAndKeyword("examplespring1", keyword);
        Hearit hearit1 = saveHearitWithTitleAndKeyword("SPRING1example", keyword1);
        Hearit hearit2 = saveHearitWithTitleAndKeyword("notitle", keyword);
        saveHearitWithTitleAndKeyword("notitle", keyword1);

        Mockito.when(hearitElasticSearchRepository.search(anyString(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(hearit.getId(), hearit1.getId(), hearit2.getId())));

        // when
        PagedResponse<HearitSearchResponse> pagedResponse = RestAssured.given(this.spec)
                .header("Authorization", "Bearer " + token)
                .queryParam("searchTerm", "spring")
                .queryParam("sort", "recommend")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/api/v2/hearits/search")
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
    @DisplayName("v2 검색 파라미터가 유효하지 않을 때 400 에러를 반환한다.")
    void readHearitsByCategoryWithInvalidParamsV2() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        String token = generateToken(member);

        // when & then (invalid sort)
        RestAssured.given(this.spec)
                .header("Authorization", "Bearer " + token)
                .queryParam("searchTerm", "spring")
                .queryParam("sort", "invalid")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/api/v2/hearits/search")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());

        // when & then (invalid page)
        RestAssured.given(this.spec)
                .header("Authorization", "Bearer " + token)
                .queryParam("searchTerm", "spring")
                .queryParam("sort", "recommend")
                .queryParam("page", -1)
                .queryParam("size", 10)
                .when()
                .get("/api/v2/hearits/search")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());

        // when & then (invalid size)
        RestAssured.given(this.spec)
                .header("Authorization", "Bearer " + token)
                .queryParam("searchTerm", "spring")
                .queryParam("sort", "recommend")
                .queryParam("page", 0)
                .queryParam("size", -1)
                .when()
                .get("/api/v2/hearits/search")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("자동완성 요청 시 200 OK 및 자동완성 결과를 반환한다.")
    void readAutocomplete() {
        // given
        Mockito.when(hearitElasticSearchRepository.autocomplete(anyString(), anyInt()))
                .thenReturn(List.of("Spring", "Spring Boot"));

        // when
        SearchAutocompleteResponse response = RestAssured.given(this.spec)
                .queryParam("searchTerm", "spring")
                .queryParam("size", 2)
                .when()
                .get("/api/v1/hearits/search/autocomplete")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(SearchAutocompleteResponse.class);

        // then
        assertThat(response.autocompletes()).containsExactly("Spring", "Spring Boot");
    }

    @Test
    @DisplayName("자동완성 size 파라미터가 유효하지 않을 때 400 에러를 반환한다.")
    void readAutocompleteWithInvalidParams() {
        // size below min (1)
        RestAssured.given(this.spec)
                .queryParam("searchTerm", "spring")
                .queryParam("size", 0)
                .when()
                .get("/api/v1/hearits/search/autocomplete")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());

        // size above max (30)
        RestAssured.given(this.spec)
                .queryParam("searchTerm", "spring")
                .queryParam("size", 31)
                .when()
                .get("/api/v1/hearits/search/autocomplete")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("자동완성 searchTerm 파라미터가 유효하지 않을 때 400 에러를 반환한다.")
    void readAutocompleteWithInvalidSearchTerm() {
        // searchTerm 누락
        RestAssured.given(this.spec)
                .queryParam("size", 5)
                .when()
                .get("/api/v1/hearits/search/autocomplete")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());

        // searchTerm 빈 문자열
        RestAssured.given(this.spec)
                .queryParam("searchTerm", "")
                .queryParam("size", 5)
                .when()
                .get("/api/v1/hearits/search/autocomplete")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());

        // searchTerm 공백만 포함
        RestAssured.given(this.spec)
                .queryParam("searchTerm", "   ")
                .queryParam("size", 5)
                .when()
                .get("/api/v1/hearits/search/autocomplete")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());

        // searchTerm 길이 초과 (51자)
        String tooLong = "a".repeat(51);
        RestAssured.given(this.spec)
                .queryParam("searchTerm", tooLong)
                .queryParam("size", 5)
                .when()
                .get("/api/v1/hearits/search/autocomplete")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    private String generateToken(Member member) {
        return jwtTokenProvider.createAccessToken(member.getUuid());
    }

    private Hearit saveHearitWithTitleAndKeyword(String title, Keyword keyword) {
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = new Hearit(
                title,
                "summary",
                100,
                "/hearit/audio/original/ORG_test.mp3",
                "/hearit/audio/short/SHR_test.mp3",
                "/hearit/script/SCR_test.json",
                List.of(new Source("출처", "url")),
                category);
        Hearit savedHearit = dbHelper.insertHearit(hearit);
        dbHelper.insertHearitKeyword(new HearitKeyword(savedHearit, keyword));
        return savedHearit;
    }
}
