package com.onair.hearit.app.presentation;

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.document;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.onair.hearit.app.dto.response.CursorResponse;
import com.onair.hearit.app.dto.response.ExploredHearitResponse;
import com.onair.hearit.app.dto.response.HearitDetailResponse;
import com.onair.hearit.app.dto.response.HearitOfCategoryResponse;
import com.onair.hearit.app.dto.response.HearitSearchResponse;
import com.onair.hearit.app.dto.response.HearitsWithRecommendCategoryResponse;
import com.onair.hearit.app.dto.response.PagedResponse;
import com.onair.hearit.app.dto.response.RecommendHearitResponse;
import com.onair.hearit.auth.infrastructure.jwt.JwtTokenProvider;
import com.onair.hearit.common.domain.Category;
import com.onair.hearit.common.domain.Hearit;
import com.onair.hearit.common.domain.HearitKeyword;
import com.onair.hearit.common.domain.Keyword;
import com.onair.hearit.common.domain.Member;
import com.onair.hearit.common.domain.PlayingHistory;
import com.onair.hearit.common.domain.RecommendHearit;
import com.onair.hearit.common.domain.Source;
import com.onair.hearit.docs.ApiDocSnippets;
import com.onair.hearit.fixture.IntegrationTest;
import com.onair.hearit.fixture.TestFixture;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import java.time.LocalDate;
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
        PlayingHistory playingHistory = dbHelper.insertPlayingHistory(
                new PlayingHistory(member.getId(), hearit, 1_000));
        Keyword keyword1 = dbHelper.insertKeyword(new Keyword("Java"));
        Keyword keyword2 = dbHelper.insertKeyword(new Keyword("Spring"));
        dbHelper.insertHearitKeyword(new HearitKeyword(hearit, keyword1));
        dbHelper.insertHearitKeyword(new HearitKeyword(hearit, keyword2));
        // when & then
        HearitDetailResponse response = RestAssured.given(this.spec)
                .header("Authorization", "Bearer " + token)
                .filter(document("hearit-read-detail",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Hearit API")
                                .summary("히어릿 상세 조회")
                                .description("히어릿의 상세 정보를 조회합니다. \n\n"
                                        + "로그인한 사용자의 경우, `isBookmarked`와 `bookmarkId` 필드가 사용자의 북마크 상태를 반영하여 반환됩니다. \n\n"
                                        + "비로그인 사용자의 경우, `isBookmarked`는 항상 `false`이며 `bookmarkId`는 `null` 입니다.")
                                .pathParameters(
                                        parameterWithName("hearitId").description("조회할 히어릿의 ID")
                                )
                                .responseSchema(Schema.schema("HearitDetailResponse"))
                                .responseFields(getHearitDetailResponseFields())
                                .build())
                ))
                .when()
                .get("/api/v1/hearits/{hearitId}", hearit.getId())
                .then().log().all()
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
        Keyword keyword1 = dbHelper.insertKeyword(new Keyword("Java"));
        dbHelper.insertHearitKeyword(new HearitKeyword(hearit, keyword1));

        // when & then
        HearitDetailResponse response = RestAssured.given(this.spec)
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
        Long notFoundHearitId = 9999L;

        // when & then
        RestAssured.given(this.spec)
                .filter(document("hearit-read-detail-not-found",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Hearit API")
                                .summary("히어릿 상세 조회")
                                .responseSchema(Schema.schema("ProblemDetail"))
                                .responseFields(ApiDocSnippets.getProblemDetailResponseFields())
                                .build())
                ))
                .when()
                .get("/api/v1/hearits/{hearitId}", notFoundHearitId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("탐색 히어릿을 조회 시, 200 OK 및 최대 10개 히어릿 정보 목록을 제공한다.")
    void readExploredHearits_byMember() {
        // given
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Keyword keyword = dbHelper.insertKeyword(new Keyword("Keyword"));

        Hearit hearit1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Hearit hearit2 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Hearit hearit3 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        dbHelper.insertHearitKeyword(new HearitKeyword(hearit1, keyword));
        dbHelper.insertHearitKeyword(new HearitKeyword(hearit2, keyword));
        dbHelper.insertHearitKeyword(new HearitKeyword(hearit3, keyword));

        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        String token = generateToken(member);

        // when
        CursorResponse<ExploredHearitResponse> responses = RestAssured.given(this.spec)
                .header("Authorization", "Bearer " + token)
                .queryParam("cursorId", 0)
                .queryParam("size", 10)
                .filter(document("hearit-read-explore-member",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Hearit API")
                                .summary("탐색 히어릿 목록 조회")
                                .description("사용자 별 최대 10개의 히어릿 목록을 조회합니다.")
                                .queryParameters(
                                        parameterWithName("cursorId").description("시작 Cursor ID").defaultValue("0"),
                                        parameterWithName("size").description("필요한 히어릿 항목 수").defaultValue("10")
                                )
                                .responseSchema(Schema.schema("CursorExploredHearitResponse"))
                                .responseFields(
                                        Stream.concat(
                                                Arrays.stream(new FieldDescriptor[]{
                                                        fieldWithPath("content[].id").description("히어릿 ID"),
                                                        fieldWithPath("content[].title").description("히어릿 제목"),
                                                        fieldWithPath("content[].categoryColorCode").description(
                                                                "카테고리 색상"),
                                                        fieldWithPath("content[].isBookmarked").description("북마크 여부"),
                                                        fieldWithPath("content[].bookmarkId").description(
                                                                "북마크 ID (북마크된 경우)").optional(),
                                                        fieldWithPath("content[].keywords").description(
                                                                "히어릿에 포함된 키워드 목록"),
                                                        fieldWithPath("content[].keywords[].id").description("키워드 ID"),
                                                        fieldWithPath("content[].keywords[].name").description(
                                                                "키워드 이름")
                                                }),
                                                Arrays.stream(ApiDocSnippets.getCustomCursorResponseFields())
                                        ).toArray(FieldDescriptor[]::new)
                                )
                                .build())
                ))
                .when()
                .get("/api/v1/hearits/explore")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(new TypeRef<>() {
                });

        // then
        assertAll(() -> {
            assertThat(responses.content()).hasSize(3);
            assertThat(responses.isEmpty()).isFalse();
            assertThat(responses.cursorId()).isEqualTo(3);
        });
    }

    @Test
    @DisplayName("오늘의 추천 히어릿을 조회 시, 200 OK 및 5개 히어릿 정보 목록을 제공한다.")
    void readRecommendedHearits() {
        // given
        LocalDate today = LocalDate.now();
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        for (int i = 0; i < 5; i++) {
            Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
            dbHelper.insertRecommendHearit(new RecommendHearit(hearit, today));
        }

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
                                        fieldWithPath("[].playTime").description("재생 시간(초)"),
                                        fieldWithPath("[].createdAt").description("생성 일시"),
                                        fieldWithPath("[].categoryName").description("카테고리 이름"),
                                        fieldWithPath("[].categoryColor").description("카테고리 색상 코드")
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
        assertThat(responses).hasSize(5);
    }

    @Test
    @DisplayName("히어릿 검색 요청 시 200 OK 및 제목 또는 키워드에 검색어가 포함된 히어릿을 최신순으로 반환한다.")
    void readHearitsByCategoryWithPagination() {
        // given
        Keyword keyword = dbHelper.insertKeyword(new Keyword("Spring"));
        Keyword keyword1 = dbHelper.insertKeyword(new Keyword("noKeyword"));

        Hearit hearit = saveHearitWithTitleAndKeyword("examplespring1", keyword);
        Hearit hearit1 = saveHearitWithTitleAndKeyword("SPRING1example", keyword1);
        Hearit hearit2 = saveHearitWithTitleAndKeyword("notitle", keyword);
        saveHearitWithTitleAndKeyword("notitle", keyword1);

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
                                                                "히어릿 마지막 재생 시간(초)").optional(),
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
        // when & then
        RestAssured.given(this.spec)
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
                .queryParam("searchTerm", "spring")
                .queryParam("page", 0)
                .queryParam("size", -1)
                .when()
                .get("/api/v1/hearits/search")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("카테고리별로 그룹화된 히어릿들을 조회 시, 추천하는 3개의 카테고리와 히어릿들을 반환한다.")
    void readHomeCategoriesHearit() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        String token = generateToken(member);

        Category category1 = dbHelper.insertCategory(new Category("Java", "#FF0000"));
        Category category2 = dbHelper.insertCategory(new Category("Spring", "#00FF00"));
        Category category3 = dbHelper.insertCategory(new Category("React1", "#0000FF"));
        Category category4 = dbHelper.insertCategory(new Category("React2", "#0000FF"));
        Category category5 = dbHelper.insertCategory(new Category("React3", "#0000FF"));
        Category category6 = dbHelper.insertCategory(new Category("React4", "#0000FF"));

        Hearit hearit11 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category1));
        Hearit hearit12 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category1));
        Hearit hearit13 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category1));
        Hearit hearit21 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category2));
        Hearit hearit22 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category2));
        Hearit hearit31 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category3));

        dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit11));
        dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit12));
        dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit13));
        dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit21));
        dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit22));
        dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit31));

        // when
        List<HearitsWithRecommendCategoryResponse> responses = RestAssured.given(this.spec)
                .header("Authorization", "Bearer " + token)
                .filter(document("hearit-recommend-category",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Hearit API")
                                .summary("추천 카테고리별 그룹화된 히어릿 조회")
                                .description(
                                        "추천하는 3개 카테고리와 카테고리별로 그룹화된 히어릿 5개 목록을 조회합니다. (현재 추천 기준 : 북마크 많은 카테고리 순, 북마크가 없는 경우 하루마다 랜덤 카테고리 추천)")
                                .responseSchema(Schema.schema("HearitsWithRecommendCategoryResponse"))
                                .responseFields(
                                        fieldWithPath("[].categoryId").description("카테고리 ID"),
                                        fieldWithPath("[].categoryName").description("카테고리 이름"),
                                        fieldWithPath("[].colorCode").description("카테고리 색상 코드"),
                                        fieldWithPath("[].hearits").description("해당 카테고리의 최신 히어릿 목록"),
                                        fieldWithPath("[].hearits[].hearitId").description("히어릿 ID"),
                                        fieldWithPath("[].hearits[].title").description("히어릿 제목"),
                                        fieldWithPath("[].hearits[].createdAt").description("히어릿 생성 일시")
                                )
                                .build()))
                )
                .when()
                .get("/api/v1/hearits/recommend-category")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getList(".", HearitsWithRecommendCategoryResponse.class);

        // then
        assertAll(() -> {
            assertThat(responses).hasSize(3);
            assertThat(responses.get(0).hearits()).hasSize(3);
            assertThat(responses.get(1).hearits()).hasSize(2);
            assertThat(responses.get(2).hearits()).hasSize(1);
            assertThat(responses.get(0).categoryId()).isEqualTo(category1.getId());
            assertThat(responses.get(1).categoryId()).isEqualTo(category2.getId());
            assertThat(responses.get(2).categoryId()).isEqualTo(category3.getId());
        });
    }

    @Test
    @DisplayName("카테고리로 히어릿 검색 시 200 OK 및 해당 카테고리의 히어릿들을 최신순으로 반환한다.")
    void searchHearitsByCategoryWithPagination() {
        // given
        Category category1 = dbHelper.insertCategory(new Category("Spring", "#000001"));
        Category category2 = dbHelper.insertCategory(new Category("Java", "#000002"));

        Keyword keyword = dbHelper.insertKeyword(TestFixture.createFixedKeyword());
        Hearit hearit1 = saveHearitWithCategoryAndKeyword(category1, keyword);
        Hearit hearit2 = saveHearitWithCategoryAndKeyword(category1, keyword);
        Hearit hearit3 = saveHearitWithCategoryAndKeyword(category2, keyword); // 카테고리 2의 히어릿

        // when
        PagedResponse<HearitOfCategoryResponse> pagedResponse = RestAssured.given(this.spec)
                .queryParam("categoryId", category1.getId())
                .queryParam("page", 0)
                .queryParam("size", 10)
                .filter(document("category-search-hearits",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Hearit API")
                                .summary("카테고리별 히어릿 목록 조회")
                                .description("특정 카테고리에 속한 히어릿 목록을 페이지별로 조회합니다.")
                                .queryParameters(
                                        parameterWithName("categoryId").description("조회할 카테고리의 ID"),
                                        parameterWithName("page").description("페이지 번호 (0부터 시작)").defaultValue("0"),
                                        parameterWithName("size").description("페이지 당 항목 수 (기본 20)").defaultValue("20")
                                )
                                .responseSchema(Schema.schema("PagedHearitSearchResponse"))
                                .responseFields(
                                        Stream.concat(
                                                Arrays.stream(new FieldDescriptor[]{
                                                        fieldWithPath("content[].id").description("히어릿 ID"),
                                                        fieldWithPath("content[].title").description("히어릿 제목"),
                                                        fieldWithPath("content[].playTime").description("히어릿 재생 시간(초)"),
                                                        fieldWithPath("content[].lastPlayTime").description(
                                                                "히어릿 마지막 재생 시간(밀리초)"),
                                                        fieldWithPath("content[].keywords[].id").description("키워드 ID"),
                                                        fieldWithPath("content[].keywords[].name").description("키워드 이름")
                                                }),
                                                Arrays.stream(ApiDocSnippets.getCustomPagedResponseFields())
                                        ).toArray(FieldDescriptor[]::new)
                                )
                                .build())
                ))
                .when()
                .get("/api/v1/hearits")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(new TypeRef<>() {
                });
        List<HearitOfCategoryResponse> responses = pagedResponse.content();

        // then
        assertAll(
                () -> assertThat(responses).hasSize(2),
                () -> assertThat(responses.get(0).id()).isEqualTo(hearit2.getId()), // 최신 hearit 먼저
                () -> assertThat(responses.get(1).id()).isEqualTo(hearit1.getId())
        );
    }

    @Test
    @DisplayName("전체 카테고리 조회 시 유효하지 않은 페이지 번호를 보내면 400 BAD_REQUEST를 반환한다.")
    void readAllCategoriesWithInvalidPage() {
        // when & then
        RestAssured.given(this.spec)
                .param("page", -1)
                .param("size", 10)
                .filter(document("category-read-list-bad-request",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Category API")
                                .summary("전체 카테고리 목록 조회")
                                .responseSchema(Schema.schema("ProblemDetail"))
                                .responseFields(ApiDocSnippets.getProblemDetailResponseFields())
                                .build())
                ))
                .when()
                .get("/api/v1/categories")
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

    private Hearit saveHearitWithCategoryAndKeyword(Category category, Keyword keyword) {
        Hearit hearit = new Hearit(
                "title",
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

    private FieldDescriptor[] getHearitDetailResponseFields() {
        return new FieldDescriptor[]{
                fieldWithPath("id").type(JsonFieldType.NUMBER).description("히어릿 ID"),
                fieldWithPath("title").type(JsonFieldType.STRING).description("히어릿 제목"),
                fieldWithPath("summary").type(JsonFieldType.STRING).description("히어릿 요약"),
                fieldWithPath("sources").description("히어릿의 출처 정보 목록"),
                fieldWithPath("sources[].sourceName").description("출처의 이름"),
                fieldWithPath("sources[].sourceUrl").description("출처의 URL"),
                fieldWithPath("playTime").type(JsonFieldType.NUMBER).description("재생 시간(초)"),
                fieldWithPath("lastPlayTime").type(JsonFieldType.NUMBER).description("마지막 재생 시간(초)").optional(),
                fieldWithPath("createdAt").type(JsonFieldType.STRING).description("생성 일시"),
                fieldWithPath("isBookmarked").type(JsonFieldType.BOOLEAN).description("현재 사용자의 북마크 여부"),
                fieldWithPath("bookmarkId").type(JsonFieldType.NUMBER).description("북마크 ID (북마크된 경우)").optional(),
                fieldWithPath("category").description("카테고리 정보"),
                fieldWithPath("category.id").type(JsonFieldType.NUMBER).description("카테고리 아이디"),
                fieldWithPath("category.name").type(JsonFieldType.STRING).description("카테고리 이름"),
                fieldWithPath("category.colorCode").type(JsonFieldType.STRING).description("카테고리 컬러코드"),
                fieldWithPath("keywords").type(JsonFieldType.ARRAY).description("키워드 목록"),
                fieldWithPath("keywords[].id").type(JsonFieldType.NUMBER).description("키워드 ID"),
                fieldWithPath("keywords[].name").type(JsonFieldType.STRING).description("키워드 이름")
        };
    }
}
