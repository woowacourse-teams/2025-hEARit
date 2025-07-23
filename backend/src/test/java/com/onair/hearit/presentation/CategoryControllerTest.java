package com.onair.hearit.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.dto.response.CategoryResponse;
import com.onair.hearit.dto.response.HearitSearchResponse;
import io.restassured.RestAssured;
import java.time.LocalDateTime;
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
                .get("/api/v1/categories")
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
    @DisplayName("카테고리로 히어릿 검색 시 200 OK 및 해당 카테고리의 히어릿들을 최신순으로 반환한다.")
    void searchHearitsByCategoryWithPagination() {
        // given
        Category category1 = saveCategory("Spring", "#001");
        Category category2 = saveCategory("Java", "#002");

        Hearit hearit1 = saveHearitWithCategory(category1);
        Hearit hearit2 = saveHearitWithCategory(category1);

        saveHearitWithCategory(category2);

        // when
        List<HearitSearchResponse> responses = RestAssured
                .given()
                .pathParam("categoryId", category1.getId())
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/api/v1/categories/{categoryId}/hearits")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getList(".", HearitSearchResponse.class);

        // then
        assertAll(
                () -> assertThat(responses).hasSize(2),
                () -> assertThat(responses.get(0).id()).isEqualTo(hearit2.getId()), // 최신 hearit 먼저
                () -> assertThat(responses.get(1).id()).isEqualTo(hearit1.getId())
        );
    }

    private Category saveCategory(String name, String color) {
        return dbHelper.insertCategory(new Category(name, color));
    }

    private Hearit saveHearitWithCategory(Category category) {
        Hearit hearit = new Hearit(
                "title",
                "summary",
                1,
                "originalAudioUrl",
                "shortAudioUrl",
                "scriptUrl",
                "source",
                LocalDateTime.now(),
                category
        );
        return dbHelper.insertHearit(hearit);
    }
}
