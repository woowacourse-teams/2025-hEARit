package com.onair.hearit.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.DbHelper;
import com.onair.hearit.common.exception.custom.NotFoundException;
import com.onair.hearit.domain.Category;
import com.onair.hearit.dto.request.CategoryListCondition;
import com.onair.hearit.dto.response.CategoryResponse;
import com.onair.hearit.infrastructure.CategoryRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import(DbHelper.class)
@ActiveProfiles("fake-test")
class CategoryServiceTest {

    @Autowired
    private DbHelper dbHelper;

    @Autowired
    private CategoryRepository categoryRepository;

    private CategoryService categoryService;

    @BeforeEach
    void setup() {
        categoryService = new CategoryService(categoryRepository);
    }

    @Test
    @DisplayName("카테고리 목록 조회 시 페이지네이션이 적용되어 반환된다.")
    void getCategories_withPagination() {
        // given
        saveCategory("category1", "#111");
        saveCategory("category2", "#222");
        saveCategory("category3", "#333");
        saveCategory("category4", "#444");
        saveCategory("category5", "#555");

        CategoryListCondition condition = new CategoryListCondition(1, 2); // page 1 (두 번째 페이지), size 2

        // when
        List<CategoryResponse> result = categoryService.getCategories(condition);

        // then
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result).extracting(CategoryResponse::name)
                        .containsExactly("category3", "category4"),
                () -> assertThat(result).extracting(CategoryResponse::colorCode)
                        .containsExactly("#333", "#444")
        );
    }


    @Test
    @DisplayName("존재하지 않는 카테고리를 조회하면 NotFoundException이 발생한다.")
    void getCategory_ById_notFound() {
        // given
        Long notExistId = 999L;

        // when & then
        assertThatThrownBy(() -> categoryService.getCategoryById(notExistId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("categoryId");
    }

    private Category saveCategory(String name, String color) {
        Category category = new Category(name, color);
        return dbHelper.insertCategory(category);
    }
}
