package com.onair.hearit.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.domain.Category;
import com.onair.hearit.dto.response.CategoryResponse;
import com.onair.hearit.dto.response.PagedResponse;
import com.onair.hearit.fixture.IntegrationTest;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

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
        PagedResponse<CategoryResponse> result = RestAssured.given()
                .param("page", 1)
                .param("size", 2)
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
}
