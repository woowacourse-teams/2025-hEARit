package com.onair.hearit.presentation;

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.document;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.onair.hearit.docs.ApiDocSnippets;
import com.onair.hearit.domain.Keyword;
import com.onair.hearit.dto.response.KeywordResponse;
import com.onair.hearit.fixture.IntegrationTest;
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
        List<KeywordResponse> result = RestAssured.given(this.spec)
                .param("page", 1)
                .param("size", 2)
                .filter(document("keyword-read-all",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Keyword API")
                                .summary("전체 키워드 목록 조회")
                                .description("전체 키워드 목록을 페이지별로 조회합니다.")
                                .queryParameters(
                                        parameterWithName("page").description("페이지 번호 (0부터 시작)"),
                                        parameterWithName("size").description("페이지 당 항목 수 (기본 20)")
                                )
                                .responseSchema(Schema.schema("KeywordResponseList"))
                                .responseFields(
                                        fieldWithPath("[].id").description("키워드 ID"),
                                        fieldWithPath("[].name").description("키워드 이름")
                                )
                                .build())
                ))
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
        KeywordResponse result = RestAssured.given(this.spec)
                .filter(document("keyword-read-single",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Keyword API")
                                .summary("단일 키워드 조회")
                                .description("ID로 특정 키워드의 정보를 조회합니다.")
                                .pathParameters(
                                        parameterWithName("keywordId").description("조회할 키워드의 ID")
                                )
                                .responseSchema(Schema.schema("KeywordResponse"))
                                .responseFields(
                                        fieldWithPath("id").description("키워드 ID"),
                                        fieldWithPath("name").description("키워드 이름")
                                )
                                .build())
                ))
                .when()
                .get("/api/v1/keywords/{keywordId}", keyword.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(KeywordResponse.class);

        // then
        assertAll(
                () -> assertThat(result.id()).isEqualTo(keyword.getId()),
                () -> assertThat(result.name()).isEqualTo(keyword.getName())
        );

    }

    @Test
    @DisplayName("존재하지 않는 키워드를 조회 시 404 NOT_FOUND를 반환한다.")
    void readNotFoundKeyword() {
        // when & then
        RestAssured.given(this.spec)
                .filter(document("keyword-read-single-not-found",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Keyword API")
                                .summary("단일 키워드 조회")
                                .responseSchema(Schema.schema("ProblemDetail"))
                                .responseFields(ApiDocSnippets.getProblemDetailResponseFields())
                                .build())
                ))
                .when()
                .get("/api/v1/keywords/{keywordId}", 9999L)
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
        List<KeywordResponse> result = RestAssured.given(this.spec)
                .param("size", size)
                .filter(document("keyword-read-recommend",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Keyword API")
                                .summary("추천 키워드 조회")
                                .description("추천 키워드를 요청한 개수만큼 랜덤으로 조회합니다.")
                                .queryParameters(
                                        parameterWithName("size").description("조회할 키워드 개수 (기본 9)")
                                )
                                .responseSchema(Schema.schema("KeywordResponseList"))
                                .responseFields(
                                        fieldWithPath("[].id").description("키워드 ID"),
                                        fieldWithPath("[].name").description("키워드 이름")
                                )
                                .build())
                ))
                .when()
                .get("/api/v1/keywords/recommend")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getList(".", KeywordResponse.class);

        // then
        assertThat(result).hasSize(size);
    }
}
