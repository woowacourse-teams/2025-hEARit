package com.onair.hearit.hearit.presentation;

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.document;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.onair.hearit.auth.infrastructure.jwt.JwtTokenProvider;
import com.onair.hearit.common.dto.response.PagedResponse;
import com.onair.hearit.fixture.ApiDocSnippets;
import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.HearitKeyword;
import com.onair.hearit.domain.Keyword;
import com.onair.hearit.domain.Member;
import com.onair.hearit.domain.Source;
import com.onair.hearit.fixture.IntegrationTest;
import com.onair.hearit.hearit.dto.HearitSearchResponse;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.restdocs.payload.FieldDescriptor;

public class HearitSearchControllerTest extends IntegrationTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("히어릿 검색 요청 시 200 OK 및 제목 또는 키워드에 검색어가 포함된 히어릿을 최신순으로 반환한다.")
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
                .filter(document("hearit-search",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Hearit API")
                                .summary("히어릿 검색")
                                .description("제목 또는 키워드에 검색어가 포함된 히어릿 목록을 페이지별로 조회합니다.")
                                .queryParameters(
                                        parameterWithName("searchTerm").description("검색어"),
                                        parameterWithName("page").description("페이지 번호 (0부터 시작)").defaultValue("0"),
                                        parameterWithName("size").description("페이지 당 항목 수").defaultValue("20")
                                )
                                .responseSchema(Schema.schema("PagedHearitSearchResponse"))
                                .responseFields(
                                        Stream.concat(
                                                Arrays.stream(new FieldDescriptor[]{
                                                        fieldWithPath("content[].id").description("히어릿 ID"),
                                                        fieldWithPath("content[].title").description("히어릿 제목"),
                                                        fieldWithPath("content[].playTime").description("히어릿 재생 시간(초)"),
                                                        fieldWithPath("content[].lastPlayTime").description(
                                                                "히어릿 마지막 재생 시간(ms)").optional(),
                                                        fieldWithPath("content[].isFinished").description(
                                                                "히어릿을 끝까지 시청했는지 여부").optional(),
                                                        fieldWithPath("content[].keywords").description(
                                                                "히어릿에 포함된 키워드 목록"),
                                                        fieldWithPath("content[].keywords[].id").description("키워드 ID"),
                                                        fieldWithPath("content[].keywords[].name").description("키워드 이름")
                                                }),
                                                Arrays.stream(ApiDocSnippets.getCustomPagedResponseFields())
                                        ).toArray(FieldDescriptor[]::new)
                                )
                                .build())
                ))
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
    void readHearitsByCategoryWithInvalidParams() {
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        String token = generateToken(member);
        // when & then
        RestAssured.given(this.spec)
                .header("Authorization", "Bearer " + token)
                .queryParam("searchTerm", "spring")
                .queryParam("page", -1)
                .queryParam("size", 10)
                .filter(document("hearit-search-bad-request",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Hearit API")
                                .summary("히어릿 검색")
                                .responseSchema(Schema.schema("ProblemDetail"))
                                .responseFields(ApiDocSnippets.getProblemDetailResponseFields())
                                .build())
                ))
                .when()
                .get("/api/v1/hearits/search")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());

        RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .queryParam("searchTerm", "spring")
                .queryParam("page", 0)
                .queryParam("size", -1)
                .when()
                .get("/api/v1/hearits/search")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    private String generateToken(Member member) {
        return jwtTokenProvider.createAccessToken(member.getId());
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
