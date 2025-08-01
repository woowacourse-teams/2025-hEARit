package com.onair.hearit.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.config.TestJpaAuditingConfig;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.HearitKeyword;
import com.onair.hearit.domain.Keyword;
import com.onair.hearit.dto.request.PagingRequest;
import com.onair.hearit.dto.response.CategoryHearitResponse;
import com.onair.hearit.dto.response.CategoryResponse;
import com.onair.hearit.dto.response.PagedResponse;
import com.onair.hearit.fixture.DbHelper;
import com.onair.hearit.fixture.TestFixture;
import com.onair.hearit.infrastructure.CategoryRepository;
import com.onair.hearit.infrastructure.HearitKeywordRepository;
import com.onair.hearit.infrastructure.HearitRepository;
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
        categoryService = new CategoryService(categoryRepository, hearitRepository, hearitKeywordRepository);
    }

    @Test
    @DisplayName("카테고리 목록 조회 시 페이지네이션이 적용되어 반환된다.")
    void getCategories_withPagination() {
        // given
        Category category = dbHelper.insertCategory(new Category("category1", "#111"));
        Category category1 = dbHelper.insertCategory(new Category("category2", "#222"));
        Category category2 = dbHelper.insertCategory(new Category("category3", "#333"));
        Category category3 = dbHelper.insertCategory(new Category("category4", "#444"));
        Category category4 = dbHelper.insertCategory(new Category("category5", "#555"));

        PagingRequest pagingRequest = new PagingRequest(1, 2);// page 1 (두 번째 페이지), size 2

        // when
        PagedResponse<CategoryResponse> result = categoryService.getCategories(pagingRequest);

        // then
        assertAll(() -> {
            assertThat(result.content()).hasSize(2);
            assertThat(result.content()).extracting(CategoryResponse::name)
                    .containsExactly("category3", "category4");
            assertThat(result.content()).extracting(CategoryResponse::colorCode)
                    .containsExactly("#333", "#444");
        });
    }

    @Test
    @DisplayName("히어릿 목록을 카테고리 조회 시 카테고리에 해당하는 히어릿만 반환한다.")
    void searchHearitsByCategory_onlyMatchingCategory() {
        // given
        Category category1 = dbHelper.insertCategory(new Category("Spring", "#001"));
        Category category2 = dbHelper.insertCategory(new Category("Java", "#002"));
        Hearit hearit1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category1));
        Hearit hearit2 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category1));
        Hearit hearit3 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category2));
        PagingRequest request = new PagingRequest(0, 10);

        // when
        PagedResponse<CategoryHearitResponse> result = categoryService.getHearitsByCategory(category1.getId(), request);

        // then
        assertAll(() -> {
            assertThat(result.content()).hasSize(2);
            assertThat(result.content()).extracting(CategoryHearitResponse::id)
                    .containsExactlyInAnyOrder(hearit2.getId(), hearit1.getId());
        });
    }

    @Test
    @DisplayName("히어릿 목록을 카테고리 조회 시 각 히어릿에 키워드가 포함되어 반환된다.")
    void searchHearitsByCategory_includesKeywords() {
        // given
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Keyword keyword1 = dbHelper.insertKeyword(TestFixture.createFixedKeyword());
        Keyword keyword2 = dbHelper.insertKeyword(TestFixture.createFixedKeyword());
        dbHelper.insertHearitKeyword(new HearitKeyword(hearit, keyword1));
        dbHelper.insertHearitKeyword(new HearitKeyword(hearit, keyword2));
        PagingRequest request = new PagingRequest(0, 10);

        // when
        PagedResponse<CategoryHearitResponse> result = categoryService.getHearitsByCategory(category.getId(), request);

        // then
        assertAll(
                () -> assertThat(result.content()).hasSize(1),
                () -> assertThat(result.content().get(0).id()).isEqualTo(hearit.getId()),
                () -> assertThat(result.content().get(0).keywords()).hasSize(2)
        );
    }

    @Test
    @DisplayName("히어릿 목록을 카테고리 조회 시 최신순으로 페이지네이션이 적용된다.")
    void searchHearitsByCategory_pagination() {
        // given
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Hearit hearit2 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Hearit hearit3 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        PagingRequest request = new PagingRequest(1, 2);

        // when
        PagedResponse<CategoryHearitResponse> result = categoryService.getHearitsByCategory(category.getId(), request);

        // then
        assertAll(
                () -> assertThat(result.content()).hasSize(1),
                () -> assertThat(result.content().get(0).id()).isEqualTo(hearit1.getId())
        );
    }
}
