package com.onair.hearit.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.DbHelper;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.HearitKeyword;
import com.onair.hearit.domain.Keyword;
import com.onair.hearit.dto.request.CategorySearchCondition;
import com.onair.hearit.dto.request.KeywordSearchCondition;
import com.onair.hearit.dto.request.TitleSearchCondition;
import com.onair.hearit.dto.response.HearitSearchResponse;
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
class HearitSearchServiceTest {

    @Autowired
    private DbHelper dbHelper;

    @Autowired
    private HearitRepository hearitRepository;

    private HearitSearchService hearitSearchService;

    @BeforeEach
    void setup() {
        hearitSearchService = new HearitSearchService(hearitRepository);
    }

    @Test
    @DisplayName("히어릿 목록을 검색으로 조회 시 제목이 포함된 히어릿만 반환한다.")
    void searchHearitsByTitle_onlyTitleMatch() {
        // given
        saveHearitByTitle("exampletitle1");
        saveHearitByTitle("title1example");
        saveHearitByTitle("wwtitle1ww");
        saveHearitByTitle("notitle");

        // when
        TitleSearchCondition condition = new TitleSearchCondition("title1", 0, 10);
        List<HearitSearchResponse> result = hearitSearchService.searchByTitle(condition);

        // then
        assertAll(
                () -> assertThat(result).hasSize(3),
                () -> assertThat(result).extracting(HearitSearchResponse::title)
                        .allSatisfy(title -> assertThat(title).contains("title1"))
        );
    }

    @Test
    @DisplayName("히어릿 목록을 검색으로 조회 시 최신순으로 정렬되어 반환된다.")
    void searchHearitsByTitle_sortedByCreatedAtDesc() {
        // given
        Hearit hearit1 = saveHearitByTitle("title1");
        Hearit hearit2 = saveHearitByTitle("title2");

        // when
        TitleSearchCondition condition = new TitleSearchCondition("title", 0, 10);
        List<HearitSearchResponse> result = hearitSearchService.searchByTitle(condition);

        // then
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result.get(0).id()).isEqualTo(hearit2.getId()),
                () -> assertThat(result.get(1).id()).isEqualTo(hearit1.getId())
        );
    }

    @Test
    @DisplayName("히어릿 목록을 검색으로 조회 시 페이지네이션이 적용되어 반환된다.")
    void searchHearitsByTitle_pagination() {
        // given
        Hearit hearit1 = saveHearitByTitle("title1");
        Hearit hearit2 = saveHearitByTitle("title2");
        Hearit hearit3 = saveHearitByTitle("title3");

        // when
        TitleSearchCondition condition = new TitleSearchCondition("title", 1, 2);
        List<HearitSearchResponse> result = hearitSearchService.searchByTitle(condition);

        // then
        assertAll(
                () -> assertThat(result).hasSize(1),
                () -> assertThat(result.get(0).id()).isEqualTo(hearit1.getId())
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
        CategorySearchCondition condition = new CategorySearchCondition(category1.getId(), 0, 10);

        // when
        List<HearitSearchResponse> result = hearitSearchService.searchByCategory(condition);

        // then
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result).extracting(HearitSearchResponse::id)
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
        CategorySearchCondition condition = new CategorySearchCondition(category.getId(), 1, 2);

        // when
        List<HearitSearchResponse> result = hearitSearchService.searchByCategory(condition);

        // then
        assertAll(
                () -> assertThat(result).hasSize(1),
                () -> assertThat(result.get(0).id()).isEqualTo(hearit1.getId())
        );
    }

    @Test
    @DisplayName("히어릿 목록을 키워드로 조회 시 해당 키워드가 포함된 히어릿만 반환한다.")
    void searchHearitsByKeyword_onlyMatchingKeyword() {
        // given
        Keyword keyword1 = saveKeyword("keyword1");
        Keyword keyword2 = saveKeyword("keyword2");

        Hearit hearit1 = saveHearitWithKeyword(keyword1);
        Hearit hearit2 = saveHearitWithKeyword(keyword1);
        Hearit hearit3 = saveHearitWithKeyword(keyword2);

        KeywordSearchCondition condition = new KeywordSearchCondition(keyword1.getId(), 0, 10);

        // when
        List<HearitSearchResponse> result = hearitSearchService.searchByKeyword(condition);

        // then
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result).extracting(HearitSearchResponse::id)
                        .containsOnly(hearit2.getId(), hearit1.getId())
        );
    }

    @Test
    @DisplayName("히어릿 목록을 키워드로 조회 시 최신순으로 페이지네이션이 적용된다.")
    void searchHearitsByKeyword_pagination() {
        // given
        Keyword keyword = saveKeyword("keyword");
        Hearit hearit1 = saveHearitWithKeyword(keyword);
        Hearit hearit2 = saveHearitWithKeyword(keyword);
        Hearit hearit3 = saveHearitWithKeyword(keyword);

        KeywordSearchCondition condition = new KeywordSearchCondition(keyword.getId(), 1, 2); // page 1, size 2

        // when
        List<HearitSearchResponse> result = hearitSearchService.searchByKeyword(condition);

        // then
        assertAll(
                () -> assertThat(result).hasSize(1),
                () -> assertThat(result.get(0).id()).isEqualTo(hearit1.getId())
        );
    }


    private Hearit saveHearitByTitle(String title) {
        Category category = new Category("category", "#123");
        dbHelper.insertCategory(category);
        Hearit hearit = new Hearit(
                title,
                "summary",
                1,
                "originalAudioUrl",
                "shortAudioUrl",
                "scriptUrl",
                "source",
                LocalDateTime.now(), category);
        return dbHelper.insertHearit(hearit);
    }

    private Category saveCategory(String name, String color) {
        Category category = new Category(name, color);
        return dbHelper.insertCategory(category);
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
                category);
        return dbHelper.insertHearit(hearit);
    }

    private Keyword saveKeyword(String name) {
        return dbHelper.insertKeyword(new Keyword(name));
    }

    private Hearit saveHearitWithKeyword(Keyword keyword) {
        Category category = saveCategory("category", "#abc");

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

        Hearit savedHearit = dbHelper.insertHearit(hearit);
        dbHelper.insertHearitKeyword(new HearitKeyword(savedHearit, keyword));
        return savedHearit;
    }
}
