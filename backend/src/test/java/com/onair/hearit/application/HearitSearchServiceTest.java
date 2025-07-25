package com.onair.hearit.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.config.TestJpaAuditingConfig;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.HearitKeyword;
import com.onair.hearit.domain.Keyword;
import com.onair.hearit.dto.request.PagingRequest;
import com.onair.hearit.dto.response.HearitSearchResponse;
import com.onair.hearit.dto.response.PagedResponse;
import com.onair.hearit.fixture.DbHelper;
import com.onair.hearit.fixture.TestFixture;
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
    @DisplayName("검색 시 제목에 검색어가 포함된 히어릿을 반환한다.")
    void searchHearitsByTitle_Success() {
        // given
        PagingRequest request = new PagingRequest(0, 10);
        Hearit hearit = saveHearitWithTitleAndKeyword("exampleSpring1", saveKeyword("keyword"));     // 제목에 검색어 포함됨
        Hearit hearit1 = saveHearitWithTitleAndKeyword("spring1example", saveKeyword("1spring1"));   // 제목에 검색어 포함됨
        Hearit hearit2 = saveHearitWithTitleAndKeyword("wwSpring1ww", saveKeyword("keyword2"));      // 제목에 검색어 포함됨
        Hearit hearit3 = saveHearitWithTitleAndKeyword("noSpring", saveKeyword("Spring"));           // 제목에 검색어 포함됨
        Hearit hearit4 = saveHearitWithTitleAndKeyword("pring", saveKeyword("sring"));               // 검색어서 제외됨
        saveHearitWithTitleAndKeyword("notitle", saveKeyword("noKeyword"));         // 검색에서 제외됨

        // when
        PagedResponse<HearitSearchResponse> result = hearitSearchService.search("Spring", request);

        // then
        assertAll(
                () -> assertThat(result.content()).hasSize(4),
                () -> assertThat(result.content()).extracting(HearitSearchResponse::id)
                        .containsExactlyInAnyOrder(hearit.getId(), hearit1.getId(), hearit2.getId(), hearit3.getId()),
                () -> assertThat(result.content()).extracting(HearitSearchResponse::id).doesNotContain(hearit4.getId())
        );
    }

    @Test
    @DisplayName("검색 시 키워드에 검색어가 포함된 히어릿을 반환한다.")
    void searchHearitsByKeyword_Succces() {
        // given
        PagingRequest request = new PagingRequest(0, 10);
        Hearit hearit = saveHearitWithTitleAndKeyword("example1", saveKeyword("Spring1"));     // 키워드에 검색어 포함됨
        Hearit hearit1 = saveHearitWithTitleAndKeyword("noTitle", saveKeyword("1springA"));    // 키워드에 검색어 포함됨
        Hearit hearit2 = saveHearitWithTitleAndKeyword("SpringS", saveKeyword("2sprINg1"));    // 키워드에 검색어 포함됨
        Hearit hearit3 = saveHearitWithTitleAndKeyword("ring", saveKeyword("SRing"));        // 검색어서 제외됨
        Hearit hearit4 = saveHearitWithTitleAndKeyword("noTitle", saveKeyword("noKeyword"));   // 검색에서 제외됨

        // when
        PagedResponse<HearitSearchResponse> result = hearitSearchService.search("Spring", request);

        // then
        assertAll(
                () -> assertThat(result.content()).hasSize(3),
                () -> assertThat(result.content()).extracting(HearitSearchResponse::id)
                        .containsExactlyInAnyOrder(hearit.getId(), hearit1.getId(), hearit2.getId()),
                () -> assertThat(result.content()).extracting(HearitSearchResponse::id)
                        .doesNotContain(hearit3.getId(), hearit4.getId())
        );
    }

    @Test
    @DisplayName("검색어가 제목 또는 키워드에 포함된 히어릿을 모두 반환한다.")
    void searchHearitsByTitleOrKeyword_Success() {
        // given
        PagingRequest request = new PagingRequest(0, 10);

        Hearit titleOnly = saveHearitWithTitleAndKeyword("spring-title", saveKeyword("nomatch")); // 제목만 매칭
        Hearit keywordOnly = saveHearitWithTitleAndKeyword("nomatch-title", saveKeyword("spring-keyword")); // 키워드만 매칭
        Hearit bothMatch = saveHearitWithTitleAndKeyword("spring-title", saveKeyword("spring-keyword")); // 둘 다 매칭
        Hearit neither = saveHearitWithTitleAndKeyword("notitle", saveKeyword("nokeyword")); // 둘 다 매칭 안 됨

        // when
        PagedResponse<HearitSearchResponse> result = hearitSearchService.search("spring", request);

        // then
        assertAll(
                () -> assertThat(result.content()).hasSize(3),
                () -> assertThat(result.content()).extracting(HearitSearchResponse::id)
                        .containsExactlyInAnyOrder(titleOnly.getId(), keywordOnly.getId(), bothMatch.getId()),
                () -> assertThat(result.content()).extracting(HearitSearchResponse::id)
                        .doesNotContain(neither.getId())
        );
    }

    @Test
    @DisplayName("히어릿 목록을 검색으로 조회 시 최신순으로 정렬되어 반환된다.")
    void searchHearitsByTitle_sortedByCreatedAtDesc() {
        // given
        PagingRequest request = new PagingRequest(0, 10);
        Hearit hearit1 = saveHearitWithTitleAndKeyword("spring1", saveKeyword("keyword"));         // oldest
        Hearit hearit2 = saveHearitWithTitleAndKeyword("notitle", saveKeyword("springKeyword"));   // middle
        Hearit hearit3 = saveHearitWithTitleAndKeyword("notitle", saveKeyword("springKeyword"));   // latest

        // when
        PagedResponse<HearitSearchResponse> result = hearitSearchService.search("Spring", request);

        // then
        assertAll(
                () -> assertThat(result.content()).hasSize(3),
                () -> assertThat(result.content().get(0).id()).isEqualTo(hearit3.getId()),
                () -> assertThat(result.content().get(1).id()).isEqualTo(hearit2.getId()),
                () -> assertThat(result.content().get(2).id()).isEqualTo(hearit1.getId())
        );
    }

    @Test
    @DisplayName("히어릿 목록을 검색으로 조회 시 페이지네이션이 적용되어 반환된다.")
    void searchHearits_pagination() {
        // given
        PagingRequest request = new PagingRequest(1, 2);
        Hearit hearit1 = saveHearitWithTitleAndKeyword("spring1", saveKeyword("keyword"));
        Hearit hearit2 = saveHearitWithTitleAndKeyword("spring2", saveKeyword("springKeyword"));
        Hearit hearit3 = saveHearitWithTitleAndKeyword("otherTitle", saveKeyword("Spring"));

        // when
        PagedResponse<HearitSearchResponse> result = hearitSearchService.search("spring", request);

        // then
        assertAll(
                () -> assertThat(result.content()).hasSize(1),
                () -> assertThat(result.content().get(0).id()).isEqualTo(hearit1.getId())
        );
    }

    private Hearit saveHearitWithTitleAndKeyword(String title, Keyword keyword) {
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = saveHearit(title, category);

        dbHelper.insertHearitKeyword(new HearitKeyword(hearit, keyword));
        return hearit;
    }

    private Hearit saveHearit(String title, Category category) {
        return dbHelper.insertHearit(new Hearit(
                title,
                "summary",
                500,
                "originalAudioUrl",
                "shortAudioUrl",
                "scriptUrl",
                "source",
                category));
    }

    private Keyword saveKeyword(String name) {
        return dbHelper.insertKeyword(new Keyword(name));
    }
}
