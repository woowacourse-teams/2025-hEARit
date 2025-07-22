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
    @DisplayName("전체 카테고리를 조회할 수 있다.")
    void getAllCategories() {
        // given
        Category category1 = saveCategory("category1", "#111");
        Category category2 = saveCategory("category2", "#222");
        Category category3 = saveCategory("category3", "#333");
        CategoryListCondition condition = new CategoryListCondition(0, 10);

        // when
        List<CategoryResponse> result = categoryService.getCategories(condition);

        // then
        assertAll(
                () -> assertThat(result).hasSize(3),
                () -> assertThat(result).extracting(CategoryResponse::id)
                        .contains(category1.getId(), category2.getId(), category3.getId())
        );
    }


    @Test
    @DisplayName("단일 카테고리를 조회할 수 있다.")
    void getCategoryById() {
        // given
        Category category = saveCategory("category", "#abc");

        // when
        CategoryResponse result = categoryService.getCategory(category.getId());

        // then
        assertThat(result.id()).isEqualTo(category.getId());
        assertThat(result.name()).isEqualTo(category.getName());
    }

    @Test
    @DisplayName("존재하지 않는 카테고리를 조회하면 NotFoundException이 발생한다.")
    void getCategory_notFound() {
        // given
        Long notExistId = 999L;

        // when & then
        assertThatThrownBy(() -> categoryService.getCategory(notExistId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("categoryId");
    }

    private Category saveCategory(String name, String color) {
        Category category = new Category(name, color);
        return dbHelper.insertCategory(category);
    }
}
