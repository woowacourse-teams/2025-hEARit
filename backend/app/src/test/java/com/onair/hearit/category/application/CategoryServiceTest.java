package com.onair.hearit.category.application;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.category.dto.CategoryResponse;
import com.onair.hearit.common.dto.request.PagingRequest;
import com.onair.hearit.common.dto.response.PagedResponse;
import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import com.onair.hearit.domain.Category;
import com.onair.hearit.fixture.DbHelper;
import com.onair.hearit.infrastructure.jpa.CategoryRepository;
import com.onair.hearit.infrastructure.jpa.HearitKeywordRepository;
import com.onair.hearit.infrastructure.jpa.HearitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import({DbHelper.class, TestJpaAuditingConfig.class})
@ActiveProfiles("fake-test")
class CategoryServiceTest {

    @Autowired
    private DbHelper dbHelper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private HearitRepository hearitRepository;

    @Autowired
    private HearitKeywordRepository hearitKeywordRepository;

    private CategoryService categoryService;

    @BeforeEach
    void setup() {
        categoryService = new CategoryService(categoryRepository);
    }

    @Test
    @DisplayName("카테고리 목록 조회 시 페이지네이션이 적용되어 반환된다.")
    void getCategories_withPagination() {
        // given
        Category category1 = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Category category2 = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Category category3 = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Category category4 = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Category category5 = dbHelper.insertCategory(TestFixture.createFixedCategory());

        PagingRequest pagingRequest = new PagingRequest(1, 2);// page 1 (두 번째 페이지), size 2

        // when
        PagedResponse<CategoryResponse> result = categoryService.getCategories(pagingRequest);

        // then
        assertAll(() -> {
            assertThat(result.content()).hasSize(2);
            assertThat(result.content()).extracting(CategoryResponse::id)
                    .containsExactly(category3.getId(), category4.getId());
        });
    }
}
