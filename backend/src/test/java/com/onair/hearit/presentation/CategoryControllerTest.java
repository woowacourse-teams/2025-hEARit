package com.onair.hearit.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.domain.Category;
import com.onair.hearit.dto.response.CategoryResponse;
import io.restassured.RestAssured;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class CategoryControllerTest extends IntegrationTest {

    @Test
    @DisplayName("전체 카테고리를 조회 시 200 OK 및 페이징이 적용된 카테고리 목록을 반환한다.")
    void readAllCategories() {
        // given
        Category category1 = saveCategory("category1", "#111");
        Category category2 = saveCategory("category2", "#222");
        Category category3 = saveCategory("category3", "#333");
        Category category4 = saveCategory("category4", "#444");
        Category category5 = saveCategory("category5", "#555");

        // when
        List<CategoryResponse> result = RestAssured.given()
                .param("page", 1)
                .param("size", 2)
                .when()
                .get("/api/v1/category")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getList(".", CategoryResponse.class);

        // then
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result).extracting(CategoryResponse::id)
                        .containsExactly(category3.getId(), category4.getId()),
                () -> assertThat(result).extracting(CategoryResponse::colorCode)
                        .containsExactly("#333", "#444")
        );
    }

    @Test
    @DisplayName("단일 카테고리 조회 시 200 OK 및 해당 카테고리 정보를 반환한다.")
    void readSingleCategory() {
        // given
        Category category = saveCategory("category", "#abc");

        // when
        CategoryResponse result = RestAssured.given()
                .when()
                .get("/api/v1/category/" + category.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(CategoryResponse.class);

        // then
        assertAll(
                () -> assertThat(result.id()).isEqualTo(category.getId()),
                () -> assertThat(result.name()).isEqualTo(category.getName()),
                () -> assertThat(result.colorCode()).isEqualTo(category.getColorCode())
        );
    }

    @Test
    @DisplayName("존재하지 않는 카테고리를 조회 시 404 NOT_FOUND를 반환한다.")
    void readNotFoundCategory() {
        // when & then
        RestAssured.given()
                .when()
                .get("/api/v1/category/999")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    private Category saveCategory(String name, String color) {
        return dbHelper.insertCategory(new Category(name, color));
    }
}
