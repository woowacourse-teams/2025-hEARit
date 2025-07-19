package com.onair.hearit.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.DbHelper;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.dto.request.TitleSearchRequest;
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
    @DisplayName("히어릿 목록을 조회 시 제목이 포함된 히어릿만 반환한다.")
    void searchHearitsByTitle_onlyTitleMatch() {
        // given
        Hearit hearit1 = saveHearitByTitle("exampletitle1");
        Hearit hearit2 = saveHearitByTitle("title1example");
        Hearit hearit3 = saveHearitByTitle("wwtitle1ww");
        Hearit hearit4 = saveHearitByTitle("notitle");

        // when
        TitleSearchRequest condition = new TitleSearchRequest("title1", 0, 10);
        List<HearitSearchResponse> result = hearitSearchService.searchByTitle(condition);

        // then
        assertAll(
                () -> assertThat(result).hasSize(3),
                () -> assertThat(result).extracting(HearitSearchResponse::title)
                        .allSatisfy(title -> assertThat(title).contains("title1"))
        );
    }

    @Test
    @DisplayName("히어릿 목록을 조회 시 최신순으로 정렬되어 반환된다.")
    void searchHearitsByTitle_sortedByCreatedAtDesc() {
        // given
        Hearit hearit1 = saveHearitByTitle("title1");
        Hearit hearit2 = saveHearitByTitle("title2");

        // when
        TitleSearchRequest condition = new TitleSearchRequest("title", 0, 10);
        List<HearitSearchResponse> result = hearitSearchService.searchByTitle(condition);

        // then
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result.get(0).id()).isEqualTo(hearit2.getId()),
                () -> assertThat(result.get(1).id()).isEqualTo(hearit1.getId())
        );
    }

    @Test
    @DisplayName("히어릿 목록을 조회 시 페이지네이션이 적용되어 반환된다.")
    void searchHearitsByTitle_pagination() {
        // given
        Hearit hearit1 = saveHearitByTitle("title1");
        Hearit hearit2 = saveHearitByTitle("title2");
        Hearit hearit3 = saveHearitByTitle("title3");

        // when
        TitleSearchRequest condition = new TitleSearchRequest("title", 1, 2);
        List<HearitSearchResponse> result = hearitSearchService.searchByTitle(condition);

        // then
        assertAll(
                () -> assertThat(result).hasSize(1),
                () -> assertThat(result.get(0).id()).isEqualTo(hearit1.getId())
        );
    }

    private Hearit saveHearitByTitle(String title) {
        Category category = new Category("category");
        dbHelper.insertCategory(category);
        Hearit hearit = new Hearit(
                title,
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
} 