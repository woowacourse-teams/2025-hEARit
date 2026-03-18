package com.onair.hearit.app.search.application;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.onair.hearit.app.common.dto.request.PagingRequest;
import com.onair.hearit.app.common.dto.response.PagedResponse;
import com.onair.hearit.app.fixture.DbHelper;
import com.onair.hearit.app.search.dto.HearitSearchResponse;
import com.onair.hearit.app.search.dto.SearchSortRequest;
import com.onair.hearit.core.config.DataSourceConfig;
import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.HearitKeyword;
import com.onair.hearit.core.domain.Keyword;
import com.onair.hearit.core.domain.Member;
import com.onair.hearit.core.domain.PlayingHistory;
import com.onair.hearit.core.domain.Source;
import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import com.onair.hearit.core.infrastructure.elasticsearch.domain.HearitSearchSortField;
import com.onair.hearit.core.infrastructure.elasticsearch.repository.HearitElasticSearchRepository;
import com.onair.hearit.core.infrastructure.jpa.PlayingHistoryRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.transaction.TestTransaction;

@DataJpaTest
@Sql("/dbclean.sql")
@ActiveProfiles("integration-test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import({DbHelper.class, TestJpaAuditingConfig.class, DataSourceConfig.class,
        HearitSearchService.class})
class HearitSearchServiceTest {

    @Autowired
    private DbHelper dbHelper;

    @Autowired
    private PlayingHistoryRepository playingHistoryRepository;

    @Autowired
    private HearitSearchService hearitSearchService;

    @MockitoBean
    private HearitElasticSearchRepository hearitElasticSearchRepository;

    @Test
    @DisplayName("검색 시 제목에 검색어가 포함된 히어릿을 반환한다.")
    void searchHearitsByTitle_Success() {
        // given
        PagingRequest request = new PagingRequest(0, 10);
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        Hearit hearit = saveHearitWithTitleAndKeyword("exampleSpring1", saveKeyword("keyword"));     // 제목에 검색어 포함됨
        Hearit hearit1 = saveHearitWithTitleAndKeyword("spring1example", saveKeyword("1spring1"));   // 제목에 검색어 포함됨
        Hearit hearit2 = saveHearitWithTitleAndKeyword("wwSpring1ww", saveKeyword("keyword2"));      // 제목에 검색어 포함됨
        Hearit hearit3 = saveHearitWithTitleAndKeyword("noSpring", saveKeyword("Spring"));           // 제목에 검색어 포함됨
        Hearit hearit4 = saveHearitWithTitleAndKeyword("pring", saveKeyword("sring"));               // 검색어서 제외됨
        saveHearitWithTitleAndKeyword("notitle", saveKeyword("noKeyword"));         // 검색에서 제외됨

        // when
        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        PagedResponse<HearitSearchResponse> result = hearitSearchService.search("Spring", request,
                TestFixture.createFixedMemberUserInfo(member));

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
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        Hearit hearit = saveHearitWithTitleAndKeyword("example1", saveKeyword("Spring1"));     // 키워드에 검색어 포함됨
        Hearit hearit1 = saveHearitWithTitleAndKeyword("noTitle", saveKeyword("1springA"));    // 키워드에 검색어 포함됨
        Hearit hearit2 = saveHearitWithTitleAndKeyword("SpringS", saveKeyword("2sprINg1"));    // 키워드에 검색어 포함됨
        Hearit hearit3 = saveHearitWithTitleAndKeyword("ring", saveKeyword("SRing"));        // 검색어서 제외됨
        Hearit hearit4 = saveHearitWithTitleAndKeyword("noTitle", saveKeyword("noKeyword"));   // 검색에서 제외됨

        // when
        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        PagedResponse<HearitSearchResponse> result = hearitSearchService.search("Spring", request,
                TestFixture.createFixedMemberUserInfo(member));

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
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        Hearit titleOnly = saveHearitWithTitleAndKeyword("spring-title", saveKeyword("nomatch")); // 제목만 매칭
        Hearit keywordOnly = saveHearitWithTitleAndKeyword("nomatch-title", saveKeyword("spring-keyword")); // 키워드만 매칭
        Hearit bothMatch = saveHearitWithTitleAndKeyword("spring-title", saveKeyword("spring-keyword")); // 둘 다 매칭
        Hearit neither = saveHearitWithTitleAndKeyword("notitle", saveKeyword("nokeyword")); // 둘 다 매칭 안 됨

        // when
        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        PagedResponse<HearitSearchResponse> result = hearitSearchService.search("spring", request,
                TestFixture.createFixedMemberUserInfo(member));

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
    @DisplayName("검색 결과에 각 히어릿에 연결된 키워드가 포함된다.")
    void searchHearitsWithKeywords_includedInResponse() {
        // given
        PagingRequest request = new PagingRequest(0, 10);
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        Keyword keyword1 = dbHelper.insertKeyword(TestFixture.createFixedKeyword());
        Keyword keyword2 = dbHelper.insertKeyword(TestFixture.createFixedKeyword());
        Hearit hearit = saveHearitWithTitleAndKeyword("Spring in Action", keyword1);

        // when
        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        PagedResponse<HearitSearchResponse> result = hearitSearchService.search("Spring", request,
                TestFixture.createFixedMemberUserInfo(member));

        // then
        assertAll(
                () -> assertThat(result.content()).hasSize(1),
                () -> assertThat(result.content().getFirst().id()).isEqualTo(hearit.getId()),
                () -> assertThat(result.content().getFirst().keywords()).hasSize(1));
    }

    @Test
    @DisplayName("히어릿 목록을 검색으로 조회 시 최신순으로 정렬되어 반환된다.")
    void searchHearitsByTitle_sortedByCreatedAtDesc() {
        // given
        PagingRequest request = new PagingRequest(0, 10);
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        Hearit hearit1 = saveHearitWithTitleAndKeyword("spring1", saveKeyword("keyword"));         // oldest
        Hearit hearit2 = saveHearitWithTitleAndKeyword("notitle", saveKeyword("springKeyword"));   // middle
        Hearit hearit3 = saveHearitWithTitleAndKeyword("notitle", saveKeyword("springKeyword"));   // latest

        // when
        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        PagedResponse<HearitSearchResponse> result = hearitSearchService.search("Spring", request,
                TestFixture.createFixedMemberUserInfo(member));

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
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        Hearit hearit1 = saveHearitWithTitleAndKeyword("spring1", saveKeyword("keyword"));
        Hearit hearit2 = saveHearitWithTitleAndKeyword("spring2", saveKeyword("springKeyword"));
        Hearit hearit3 = saveHearitWithTitleAndKeyword("otherTitle", saveKeyword("Spring"));
        TestTransaction.flagForCommit();
        TestTransaction.end();

        // when
        TestTransaction.start();

        PagedResponse<HearitSearchResponse> result = hearitSearchService.search(
                "spring", request, TestFixture.createFixedMemberUserInfo(member));

        // then
        assertAll(
                () -> assertThat(result.content()).hasSize(1),
                () -> assertThat(result.content().getFirst().id()).isEqualTo(hearit1.getId())
        );
    }

    @Test
    @DisplayName("V2 검색은 ElasticSearch가 반환한 ID 순서를 그대로 유지해서 히어릿을 반환한다.")
    void searchHearitsV2_keepsOrderFromElasticSearch() {
        // given
        PagingRequest request = new PagingRequest(0, 10);
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());

        Hearit h1 = saveHearitWithTitleAndKeyword("spring1", saveKeyword("keyword"));
        Hearit h2 = saveHearitWithTitleAndKeyword("spring2", saveKeyword("keyword"));
        Hearit h3 = saveHearitWithTitleAndKeyword("spring3", saveKeyword("keyword"));

        // ElasticSearch가 최신/추천 등 정렬을 수행했다고 가정하고, 특정 순서로 ID를 내려준다.
        Pageable pageable = PageRequest.of(request.page(), request.size());
        when(hearitElasticSearchRepository.search(eq("spring"),
                eq(HearitSearchSortField.RECOMMENDED),
                eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(h3.getId(), h1.getId(), h2.getId()), pageable, 3));

        SearchSortRequest sortRequest = Mockito.mock(SearchSortRequest.class);
        when(sortRequest.field()).thenReturn(HearitSearchSortField.RECOMMENDED);

        // when
        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        PagedResponse<HearitSearchResponse> result = hearitSearchService.searchV2(
                "spring", sortRequest, request, TestFixture.createFixedMemberUserInfo(member));

        // then
        assertAll(
                () -> assertThat(result.content()).hasSize(3),
                () -> assertThat(result.content()).extracting(HearitSearchResponse::id)
                        .containsExactly(h3.getId(), h1.getId(), h2.getId())
        );
    }

    @Test
    @DisplayName("V2 검색은 정렬 필드 값을 ElasticSearch 검색에 전달한다.")
    void searchHearitsV2_passesSortFieldToElasticSearch() {
        // given
        PagingRequest request = new PagingRequest(0, 10);
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        Hearit h1 = saveHearitWithTitleAndKeyword("spring1", saveKeyword("keyword"));

        Pageable pageable = PageRequest.of(request.page(), request.size());
        when(hearitElasticSearchRepository.search(anyString(),
                any(HearitSearchSortField.class),
                any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(h1.getId()), pageable, 1));

        SearchSortRequest sortRequest = Mockito.mock(SearchSortRequest.class);
        when(sortRequest.field()).thenReturn(HearitSearchSortField.LATEST);

        // when
        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        hearitSearchService.searchV2(
                "spring", sortRequest, request, TestFixture.createFixedMemberUserInfo(member));

        // then
        verify(hearitElasticSearchRepository).search(eq("spring"),
                eq(HearitSearchSortField.LATEST),
                eq(pageable));
    }

    @DisplayName("검색 시 시청기록 정보(마지막 재생시간, 끝까지 시청했는지 여부)도 함께 제공한다.")
    @Test
    void searchHearit_withPlayingHistory() {
        // given
        PagingRequest pagingRequest = new PagingRequest(0, 10);
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        Hearit hearit = saveHearitWithTitleAndKeyword("spring test title", saveKeyword("keyword"));

        PlayingHistory playingHistory = playingHistoryRepository.save(
                new PlayingHistory(member.getUuid(), hearit, 10L));

        TestTransaction.flagForCommit();
        TestTransaction.end();

        // when
        TestTransaction.start();
        PagedResponse<HearitSearchResponse> result = hearitSearchService.search(
                "spring", pagingRequest, TestFixture.createFixedMemberUserInfo(member));

        // then
        HearitSearchResponse hearitSearchResponse = result.content().getFirst();
        assertAll(
                () -> assertThat(hearitSearchResponse.id()).isEqualTo(hearit.getId()),
                () -> assertThat(hearitSearchResponse.lastPlayTime()).isEqualTo(playingHistory.getLastPlayTime()),
                () -> assertThat(hearitSearchResponse.isFinished()).isEqualTo(playingHistory.isFinished())
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
                "/hearit/audio/original/ORG_test.mp3",
                "/hearit/audio/short/SHR_test.mp3",
                "/hearit/script/SCR_test.json",
                List.of(new Source("출처", "url")),
                category));
    }

    private Keyword saveKeyword(String name) {
        return dbHelper.insertKeyword(new Keyword(name));
    }
}
