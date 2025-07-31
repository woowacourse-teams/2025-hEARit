package com.onair.hearit.presentation;

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.document;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.onair.hearit.auth.infrastructure.jwt.JwtTokenProvider;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.HearitKeyword;
import com.onair.hearit.domain.Keyword;
import com.onair.hearit.domain.Member;
import com.onair.hearit.dto.response.HearitDetailResponse;
import com.onair.hearit.dto.response.HearitSearchResponse;
import com.onair.hearit.dto.response.PagedResponse;
import com.onair.hearit.dto.response.RandomHearitResponse;
import com.onair.hearit.dto.response.RecommendHearitResponse;
import com.onair.hearit.fixture.IntegrationTest;
import com.onair.hearit.fixture.TestFixture;
import com.onair.hearit.utils.ApiDocumentUtils;
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
import org.springframework.restdocs.payload.JsonFieldType;

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
        HearitDetailResponse response = RestAssured.given(this.spec)
                .header("Authorization", "Bearer " + token)
                .filter(document("hearit-read-detail-member",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Hearit API")
                                .summary("히어릿 상세 조회 (로그인)")
                                .description("로그인한 사용자가 히어릿의 상세 정보를 조회합니다. 북마크 여부가 포함됩니다.")
                                .requestHeaders(ApiDocumentUtils.getAuthorizationHeader())
                                .pathParameters(
                                        parameterWithName("hearitId").description("조회할 히어릿의 ID")
                                )
                                .responseSchema(Schema.schema("HearitDetailResponse"))
                                .responseFields(getHearitDetailResponseFields())
                                .build())
                ))
                .when()
                .get("/api/v1/hearits/{hearitId}", hearit.getId())
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
        HearitDetailResponse response = RestAssured.given(this.spec)
                .filter(document("hearit-read-detail-not-member",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Hearit API")
                                .summary("히어릿 상세 조회 (비로그인)")
                                .description("비로그인 사용자가 히어릿의 상세 정보를 조회합니다. 북마크 여부는 항상 false입니다.")
                                .pathParameters(
                                        parameterWithName("hearitId").description("조회할 히어릿의 ID")
                                )
                                .responseSchema(Schema.schema("HearitDetailResponse"))
                                .responseFields(getHearitDetailResponseFields())
                                .build())
                ))
                .when()
                .get("/api/v1/hearits/{hearitId}", hearit.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract().as(HearitDetailResponse.class);

        assertAll(
                () -> assertThat(response.id()).isEqualTo(hearit.getId()),
                () -> assertThat(response.isBookmarked()).isFalse()
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
        PagedResponse<RandomHearitResponse> responses = RestAssured.given(this.spec)
                .filter(document("hearit-read-random",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Hearit API")
                                .summary("랜덤 히어릿 목록 조회")
                                .description("랜덤으로 최대 10개의 히어릿 목록을 조회합니다.")
                                .responseSchema(Schema.schema("PagedRandomHearitResponse"))
                                .responseFields(
                                        Stream.concat(
                                                Arrays.stream(new FieldDescriptor[]{
                                                        fieldWithPath("content[].id").description("히어릿 ID"),
                                                        fieldWithPath("content[].title").description("히어릿 제목"),
                                                        fieldWithPath("content[].summary").description("히어릿 요약"),
                                                        fieldWithPath("content[].source").description("출처"),
                                                        fieldWithPath("content[].playTime").description("재생 시간(초)"),
                                                        fieldWithPath("content[].createdAt").description("생성 일시"),
                                                        fieldWithPath("content[].isBookmarked").description("북마크 여부"),
                                                        fieldWithPath("content[].bookmarkId").description(
                                                                "북마크 ID (북마크된 경우)").optional()
                                                }),
                                                Arrays.stream(ApiDocumentUtils.getCustomPagedResponseFields())
                                        ).toArray(FieldDescriptor[]::new)
                                )
                                .build())
                ))
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
        List<RecommendHearitResponse> responses = RestAssured.given(this.spec)
                .filter(document("hearit-read-recommend",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Hearit API")
                                .summary("추천 히어릿 목록 조회")
                                .description("추천 히어릿 목록을 최대 5개까지 조회합니다.")
                                .responseSchema(Schema.schema("RecommendHearitResponseList"))
                                .responseFields(
                                        fieldWithPath("[].id").description("히어릿 ID"),
                                        fieldWithPath("[].title").description("히어릿 제목"),
                                        fieldWithPath("[].summary").description("히어릿 요약"),
                                        fieldWithPath("[].source").description("출처"),
                                        fieldWithPath("[].playTime").description("재생 시간(초)"),
                                        fieldWithPath("[].createdAt").description("생성 일시")
                                )
                                .build())
                ))
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
        PagedResponse<HearitSearchResponse> pagedResponse = RestAssured.given(this.spec)
                .queryParam("searchTerm", "spring")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .filter(document("hearit-search",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Hearit API")
                                .summary("히어릿 검색")
                                .description("제목 또는 키워드에 검색어가 포함된 히어릿 목록을 페이지별로 조회합니다.")
                                .queryParameters(
                                        parameterWithName("searchTerm").description("검색어 (2자 이상)"),
                                        parameterWithName("page").description("페이지 번호 (0부터 시작)"),
                                        parameterWithName("size").description("페이지 당 항목 수")
                                )
                                .responseSchema(Schema.schema("PagedHearitSearchResponse"))
                                .responseFields(
                                        Stream.concat(
                                                Arrays.stream(new FieldDescriptor[]{
                                                        fieldWithPath("content[].id").description("히어릿 ID"),
                                                        fieldWithPath("content[].title").description("히어릿 제목"),
                                                        fieldWithPath("content[].summary").description("히어릿 요약"),
                                                        fieldWithPath("content[].playTime").description("히어릿 재생 시간(초)")
                                                }),
                                                Arrays.stream(ApiDocumentUtils.getCustomPagedResponseFields())
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

    private FieldDescriptor[] getHearitDetailResponseFields() {
        return new FieldDescriptor[]{
                fieldWithPath("id").type(JsonFieldType.NUMBER).description("히어릿 ID"),
                fieldWithPath("title").type(JsonFieldType.STRING).description("히어릿 제목"),
                fieldWithPath("summary").type(JsonFieldType.STRING).description("히어릿 요약"),
                fieldWithPath("source").type(JsonFieldType.STRING).description("출처"),
                fieldWithPath("playTime").type(JsonFieldType.NUMBER).description("재생 시간(초)"),
                fieldWithPath("createdAt").type(JsonFieldType.STRING).description("생성 일시"),
                fieldWithPath("isBookmarked").type(JsonFieldType.BOOLEAN).description("현재 사용자의 북마크 여부"),
                fieldWithPath("bookmarkId").type(JsonFieldType.NUMBER).description("북마크 ID (북마크된 경우)").optional()
        };
    }
}
