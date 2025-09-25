package com.onair.hearit.category.presentation;

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.document;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.onair.hearit.category.dto.CategoryResponse;
import com.onair.hearit.common.dto.response.PagedResponse;
import com.onair.hearit.core.docs.ApiDocSnippets;
import com.onair.hearit.domain.Category;
import com.onair.hearit.fixture.IntegrationTest;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import java.util.Arrays;
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
        Category category1 = dbHelper.insertCategory(new Category("category1", "#111111"));
        Category category2 = dbHelper.insertCategory(new Category("category2", "#222222"));
        Category category3 = dbHelper.insertCategory(new Category("category3", "#333333"));
        Category category4 = dbHelper.insertCategory(new Category("category4", "#444444"));
        Category category5 = dbHelper.insertCategory(new Category("category5", "#555555"));

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
                                        parameterWithName("page").description("페이지 번호 (0부터 시작)").defaultValue("0"),
                                        parameterWithName("size").description("페이지 당 항목 수 (기본 20)").defaultValue("20")
                                )
                                .responseSchema(Schema.schema("PagedResponse"))
                                .responseFields(
                                        Stream.concat(
                                                Arrays.stream(new FieldDescriptor[]{
                                                        fieldWithPath("content[].id").description("카테고리 ID"),
                                                        fieldWithPath("content[].name").description("카테고리 이름"),
                                                        fieldWithPath("content[].colorCode").description("카테고리 색상 코드")
                                                }),
                                                Arrays.stream(ApiDocSnippets.getCustomPagedResponseFields())
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
                        .containsExactly("#333333", "#444444")
        );
    }

    @Test
    @DisplayName("전체 카테고리 조회 시 유효하지 않은 페이지 번호를 보내면 400 BAD_REQUEST를 반환한다.")
    void readAllCategoriesWithInvalidPage() {
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
}
