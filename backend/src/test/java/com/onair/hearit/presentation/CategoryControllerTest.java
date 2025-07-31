package com.onair.hearit.presentation;

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.document;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.dto.response.CategoryResponse;
import com.onair.hearit.dto.response.HearitSearchResponse;
import com.onair.hearit.dto.response.PagedResponse;
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
import org.springframework.http.HttpStatus;
import org.springframework.restdocs.payload.FieldDescriptor;

class CategoryControllerTest extends IntegrationTest {

    @Test
    @DisplayName("전체 카테고리를 조회 시 200 OK 및 페이징이 적용된 카테고리 목록을 반환한다.")
    void readAllCategories() {
        // given
        Category category1 = dbHelper.insertCategory(new Category("category1", "#111"));
        Category category2 = dbHelper.insertCategory(new Category("category2", "#222"));
        Category category3 = dbHelper.insertCategory(new Category("category3", "#333"));
        Category category4 = dbHelper.insertCategory(new Category("category4", "#444"));
        Category category5 = dbHelper.insertCategory(new Category("category5", "#555"));

        // when
        PagedResponse<CategoryResponse> result = RestAssured.given(this.spec)
                .param("page", 1)
                .param("size", 2)
                .filter(document("category-read-list",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Category API")
                                .summary("전체 카테고리 목록 조회")
                                .description("전체 카테고리 목록을 페이지별로 조회합니다.")
                                .queryParameters(
                                        parameterWithName("page").description("페이지 번호 (0부터 시작)"),
                                        parameterWithName("size").description("페이지 당 항목 수 (기본 20)")
                                )
                                .responseSchema(Schema.schema("PagedResponse"))
                                .responseFields(
                                        Stream.concat(
                                                Arrays.stream(new FieldDescriptor[]{
                                                        fieldWithPath("content[].id").description("카테고리 ID"),
                                                        fieldWithPath("content[].name").description("카테고리 이름"),
                                                        fieldWithPath("content[].colorCode").description("카테고리 색상 코드")
                                                }),
                                                Arrays.stream(ApiDocumentUtils.getCustomPagedResponseFields())
                                        ).toArray(FieldDescriptor[]::new)
                                )
                                .build())
                ))
                .when()
                .get("/api/v1/categories")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(new TypeRef<>() {
                });

        // then
        assertAll(
                () -> assertThat(result.content()).hasSize(2),
                () -> assertThat(result.content()).extracting(CategoryResponse::id)
                        .containsExactly(category3.getId(), category4.getId()),
                () -> assertThat(result.content()).extracting(CategoryResponse::colorCode)
                        .containsExactly("#333", "#444")
        );
    }

    @Test
    @DisplayName("카테고리로 히어릿 검색 시 200 OK 및 해당 카테고리의 히어릿들을 최신순으로 반환한다.")
    void searchHearitsByCategoryWithPagination() {
        // given
        Category category1 = dbHelper.insertCategory(new Category("Spring", "#001"));
        Category category2 = dbHelper.insertCategory(new Category("Java", "#002"));

        Hearit hearit1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category1));
        Hearit hearit2 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category1));
        Hearit hearit3 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category2)); // 카테고리 2의 히어릿

        // when
        PagedResponse<HearitSearchResponse> pagedResponse = RestAssured.given(this.spec)
                .pathParam("categoryId", category1.getId())
                .queryParam("page", 0)
                .queryParam("size", 10)
                .filter(document("category-search-hearits",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Category API")
                                .summary("카테고리별 히어릿 목록 조회")
                                .description("특정 카테고리에 속한 히어릿 목록을 페이지별로 조회합니다.")
                                .pathParameters(
                                        parameterWithName("categoryId").description("조회할 카테고리의 ID")
                                )
                                .queryParameters(
                                        parameterWithName("page").description("페이지 번호 (0부터 시작)"),
                                        parameterWithName("size").description("페이지 당 항목 수 (기본 20)")
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
                .get("/api/v1/categories/{categoryId}/hearits")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(new TypeRef<>() {
                });
        List<HearitSearchResponse> responses = pagedResponse.content();

        // then
        assertAll(
                () -> assertThat(responses).hasSize(2),
                () -> assertThat(responses.get(0).id()).isEqualTo(hearit2.getId()), // 최신 hearit 먼저
                () -> assertThat(responses.get(1).id()).isEqualTo(hearit1.getId())
        );
    }
}
