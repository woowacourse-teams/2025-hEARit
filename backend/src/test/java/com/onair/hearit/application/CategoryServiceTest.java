package com.onair.hearit.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.DbHelper;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.dto.request.PagingRequest;
import com.onair.hearit.dto.response.CategoryResponse;
import com.onair.hearit.dto.response.HearitSearchResponse;
import com.onair.hearit.dto.response.PagedResponse;
import com.onair.hearit.infrastructure.CategoryRepository;
import com.onair.hearit.infrastructure.HearitRepository;
import java.time.LocalDateTime;
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

    @Autowired
    private HearitRepository hearitRepository;

    private CategoryService categoryService;

    @BeforeEach
    void setup() {
        categoryService = new CategoryService(categoryRepository, hearitRepository);
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

        PagingRequest pagingRequest = new PagingRequest(1, 2);// page 1 (두 번째 페이지), size 2

        // when
        List<CategoryResponse> result = categoryService.getCategories(pagingRequest);

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
    @DisplayName("히어릿 목록을 카테고리 조회 시 카테고리에 해당하는 히어릿만 반환한다.")
    void searchHearitsByCategory_onlyMatchingCategory() {
        // given
        Category category1 = saveCategory("Spring", "#001");
        Category category2 = saveCategory("Java", "#002");
        Hearit hearit1 = saveHearitWithCategory(category1);
        Hearit hearit2 = saveHearitWithCategory(category1);
        saveHearitWithCategory(category2);
        PagingRequest request = new PagingRequest(0, 10);

        // when
        PagedResponse<HearitSearchResponse> result = categoryService.findHearitsByCategory(category1.getId(), request);

        // then
        assertAll(
                () -> assertThat(result.content()).hasSize(2),
                () -> assertThat(result.content()).extracting(HearitSearchResponse::id)
                        .containsExactlyInAnyOrder(hearit2.getId(), hearit1.getId())
        );
    }

    @Test
    @DisplayName("히어릿 목록을 카테고리 조회 시 최신순으로 페이지네이션이 적용된다.")
    void searchHearitsByCategory_pagination() {
        // given
        Category category = saveCategory("Spring", "#001");
        Hearit hearit1 = saveHearitWithCategory(category);
        Hearit hearit2 = saveHearitWithCategory(category);
        Hearit hearit3 = saveHearitWithCategory(category);
        PagingRequest request = new PagingRequest(1, 2);

        // when
        PagedResponse<HearitSearchResponse> result = categoryService.findHearitsByCategory(category.getId(), request);

        // then
        assertAll(
                () -> assertThat(result.content()).hasSize(1),
                () -> assertThat(result.content().get(0).id()).isEqualTo(hearit1.getId())
        );
    }

    private Category saveCategory(String name, String color) {
        Category category = new Category(name, color);
        return dbHelper.insertCategory(category);
    }

    private Hearit saveHearitWithCategory(Category category) {
        Hearit hearit = new Hearit("title", "summary", 1, "originalAudioUrl", "shortAudioUrl", "scriptUrl", "source",
                LocalDateTime.now(), category);
        return dbHelper.insertHearit(hearit);
    }
}
