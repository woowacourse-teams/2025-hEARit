package com.onair.hearit.app.hearit.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.app.common.dto.request.PagingRequest;
import com.onair.hearit.app.common.dto.response.PagedResponse;
import com.onair.hearit.app.exception.custom.NotFoundException;
import com.onair.hearit.app.fixture.DbHelper;
import com.onair.hearit.app.hearit.dto.HearitDetailResponse;
import com.onair.hearit.app.hearit.dto.FilteredHearitResponse;
import com.onair.hearit.app.hearit.dto.HearitSortField;
import com.onair.hearit.app.hearit.dto.HearitSortRequest;
import com.onair.hearit.app.hearit.dto.HearitsWithRecommendCategoryResponse;
import com.onair.hearit.app.hearit.dto.SortDirection;
import com.onair.hearit.core.domain.Bookmark;
import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.HearitKeyword;
import com.onair.hearit.core.domain.Keyword;
import com.onair.hearit.core.domain.Member;
import com.onair.hearit.core.domain.PlayingHistory;
import com.onair.hearit.core.domain.Source;
import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import com.onair.hearit.core.infrastructure.jpa.BookmarkRepository;
import com.onair.hearit.core.infrastructure.jpa.CategoryRepository;
import com.onair.hearit.core.infrastructure.jpa.HearitKeywordRepository;
import com.onair.hearit.core.infrastructure.jpa.HearitRepository;
import com.onair.hearit.core.infrastructure.jpa.MemberRepository;
import com.onair.hearit.core.infrastructure.jpa.PlayingHistoryRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("fake-test")
@Import({DbHelper.class, TestJpaAuditingConfig.class})
class HearitServiceTest {

    @Autowired
    private DbHelper dbHelper;

    @Autowired
    private HearitRepository hearitRepository;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private HearitKeywordRepository hearitKeywordRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PlayingHistoryRepository playingHistoryRepository;

    private HearitService hearitService;

    @BeforeEach
    void setup() {
        hearitService = new HearitService(
                hearitRepository,
                memberRepository,
                bookmarkRepository,
                hearitKeywordRepository,
                categoryRepository,
                playingHistoryRepository);
    }

    @Test
    @DisplayName("히어릿 아이디로 단일 히어릿 정보를 조회 할 수 있다.")
    void getHearitDetailTest() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Bookmark bookmark = dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit));
        Keyword keyword = dbHelper.insertKeyword(TestFixture.createFixedKeyword());
        HearitKeyword hearitKeyword = dbHelper.insertHearitKeyword(new HearitKeyword(hearit, keyword));

        // when
        HearitDetailResponse response = hearitService.getHearitDetail(hearit.getId(),
                TestFixture.createFixedMemberUserInfo(member));

        // then
        assertAll(() -> {
            assertThat(response.id()).isEqualTo(hearit.getId());
            assertThat(response.title()).isEqualTo(hearit.getTitle());
            assertThat(response.summary()).isEqualTo(hearit.getSummary());
            assertThat(response.isBookmarked()).isTrue();
            assertThat(response.bookmarkId()).isEqualTo(bookmark.getId());
            assertThat(response.category().id()).isEqualTo(hearit.getCategory().getId());
            assertThat(response.category().name()).isEqualTo(hearit.getCategory().getName());
            assertThat(response.keywords()).hasSize(1);
        });
    }

    @Nested
    @DisplayName("히어릿 단일조회 시 lastPlayTime 초기화 규칙")
    class LastPlayTimeResetTest {

        @ParameterizedTest
        @ValueSource(longs = {5000L, 1000L, 0L})
        @DisplayName("lastPlayTime이 5000ms 이내로 저장된 경우 lastPlayTime은 0ms으로 초기화된다.")
        void resetLastPlayTimeToZero_whenWithin5Seconds(long remainingSeconds) {
            // given
            Member member = dbHelper.insertMember(TestFixture.createFixedMember());
            Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
            Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

            long lastPlayTime = hearit.getPlayTime() * 1000 - remainingSeconds;
            playingHistoryRepository.save(new PlayingHistory(member.getId(), hearit, lastPlayTime));

            // when
            HearitDetailResponse response = hearitService.getHearitDetail(
                    hearit.getId(),
                    TestFixture.createFixedMemberUserInfo(member)
            );

            // then
            assertAll(
                    () -> assertThat(response.id()).isEqualTo(hearit.getId()),
                    () -> assertThat(response.lastPlayTime()).isEqualTo(0L)
            );
        }

        @Test
        @DisplayName("lastPlayTime이 5000ms 초과로 저장된 경우 lastPlayTime은 그대로 유지된다.")
        void keepLastPlayTime_whenExceeds5Seconds() {
            // given
            Member member = dbHelper.insertMember(TestFixture.createFixedMember());
            Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
            Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category)); //히어릿 playTime 500초

            long remainingSeconds = 5001L;
            Long lastPlayTime = hearit.getPlayTime() * 1000 - remainingSeconds;
            playingHistoryRepository.save(new PlayingHistory(member.getId(), hearit, lastPlayTime));

            // when
            HearitDetailResponse response = hearitService.getHearitDetail(
                    hearit.getId(),
                    TestFixture.createFixedMemberUserInfo(member)
            );

            // then
            assertAll(
                    () -> assertThat(response.id()).isEqualTo(hearit.getId()),
                    () -> assertThat(response.lastPlayTime()).isEqualTo(lastPlayTime)
            );
        }
    }

    @Test
    @DisplayName("존재하지 않는 히어릿 아이디로 단일 히어릿 조회 시 NoFoundException을 던진다.")
    void getHearitDetailNotFoundTest() {
        // given
        Long notExistHearitId = 1L;
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());

        // when & then
        assertThatThrownBy(() -> hearitService.getHearitDetail(notExistHearitId,
                TestFixture.createFixedMemberUserInfo(member)))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("hearitId");
    }

    @Test
    @DisplayName("북마크한 카테고리가 3개 이상인 경우, 북마크 개수별로 정렬하여 3개의 카테고리와 히어릿들을 조회할 수 있다.")
    void getHearitsWithRecommendCategory() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());

        Category c1 = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Category c2 = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Category c3 = dbHelper.insertCategory(TestFixture.createFixedCategory());

        Hearit hearit11 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(c1));
        Hearit hearit12 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(c1));
        Hearit hearit13 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(c1));
        Hearit hearit21 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(c2));
        Hearit hearit22 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(c2));
        Hearit hearit31 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(c3));

        dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit11));
        dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit12));
        dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit13));
        dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit21));
        dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit22));
        dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit31));

        // when
        List<HearitsWithRecommendCategoryResponse> responses = hearitService.getHearitsWithRecommendCategory(
                TestFixture.createFixedMemberUserInfo(member));

        // then
        assertAll(() -> {
            assertThat(responses).hasSize(3);
            assertThat(responses.get(0).hearits()).hasSize(3);
            assertThat(responses.get(1).hearits()).hasSize(2);
            assertThat(responses.get(2).hearits()).hasSize(1);
            assertThat(responses.get(0).categoryId()).isEqualTo(c1.getId());
            assertThat(responses.get(1).categoryId()).isEqualTo(c2.getId());
            assertThat(responses.get(2).categoryId()).isEqualTo(c3.getId());
        });
    }

    @Test
    @DisplayName("북마크한 카테고리가 n개(3개 미만)인 경우, 오늘의 랜덤 카테고리 3-n개와 북마크한 카테고리 n개의 히어릿들을 조회할 수 있다.")
    void getHearitsWithRecommendCategory_TodayRandom() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());

        Category category1 = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Category category2 = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Category category3 = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Category category4 = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Category category5 = dbHelper.insertCategory(TestFixture.createFixedCategory());

        Hearit hearit11 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category1));
        Hearit hearit12 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category1));
        Hearit hearit13 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category1));

        dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit11));
        dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit12));
        dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit13));

        // when
        List<HearitsWithRecommendCategoryResponse> firstResponses = hearitService.getHearitsWithRecommendCategory(
                TestFixture.createFixedMemberUserInfo(member));
        List<HearitsWithRecommendCategoryResponse> secondResponses = hearitService.getHearitsWithRecommendCategory(
                TestFixture.createFixedMemberUserInfo(member));

        // then
        assertAll(() -> {
            assertThat(firstResponses.get(0).categoryId()).isEqualTo(category1.getId());
            assertThat(secondResponses.get(0).categoryId()).isEqualTo(category1.getId());
            assertThat(firstResponses.get(1).categoryId()).isEqualTo(secondResponses.get(1).categoryId());
            assertThat(firstResponses.get(2).categoryId()).isEqualTo(secondResponses.get(2).categoryId());
        });
    }

    @Test
    @DisplayName("히어릿 목록을 카테고리 조회 시 카테고리에 해당하는 히어릿만 반환한다.")
    void getHearitsByCategory_onlyMatchingCategory() {
        // given
        Category category1 = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Category category2 = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category1));
        Hearit hearit2 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category1));
        Hearit hearit3 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category2));

        HearitSortRequest sortRequest = new HearitSortRequest(HearitSortField.CREATED_AT, SortDirection.DESC);
        PagingRequest pagingRequest = new PagingRequest(0, 10);

        // when
        PagedResponse<FilteredHearitResponse> result = hearitService.getFilteredHearits(category1.getId(),
                sortRequest, TestFixture.createFixedGuestUserInfo(UUID.randomUUID().toString()), pagingRequest);

        // then
        assertAll(() -> {
            assertThat(result.content()).hasSize(2);
            assertThat(result.content()).extracting(FilteredHearitResponse::id)
                    .containsExactlyInAnyOrder(hearit2.getId(), hearit1.getId());
        });
    }

    @Test
    @DisplayName("카테고리 내의 히어릿들을 조회 시 각 히어릿에 키워드가 포함되어 반환된다.")
    void getHearitsByCategory_includesKeywords() {
        // given
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Keyword keyword1 = dbHelper.insertKeyword(TestFixture.createFixedKeyword());
        Keyword keyword2 = dbHelper.insertKeyword(TestFixture.createFixedKeyword());
        dbHelper.insertHearitKeyword(new HearitKeyword(hearit, keyword1));
        dbHelper.insertHearitKeyword(new HearitKeyword(hearit, keyword2));

        HearitSortRequest sortRequest = new HearitSortRequest(HearitSortField.CREATED_AT, SortDirection.DESC);
        PagingRequest pagingRequest = new PagingRequest(0, 10);

        // when
        PagedResponse<FilteredHearitResponse> result = hearitService.getFilteredHearits(category.getId(),
                sortRequest, TestFixture.createFixedGuestUserInfo(UUID.randomUUID().toString()), pagingRequest);

        // then
        assertAll(() -> {
            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0).id()).isEqualTo(hearit.getId());
            assertThat(result.content().get(0).keywords()).hasSize(2);
        });
    }

    @Test
    @DisplayName("카테고리 내의 히어릿들을 조회 시 최신순으로 페이지네이션이 적용된다.")
    void getHearitsByCategory_pagination() {
        // given
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Hearit hearit2 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Hearit hearit3 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        HearitSortRequest sortRequest = new HearitSortRequest(HearitSortField.CREATED_AT, SortDirection.DESC);
        PagingRequest pagingRequest = new PagingRequest(1, 2);

        // when
        PagedResponse<FilteredHearitResponse> result = hearitService.getFilteredHearits(category.getId(),
                sortRequest, TestFixture.createFixedGuestUserInfo(UUID.randomUUID().toString()), pagingRequest);

        // then
        assertAll(() -> {
            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0).id()).isEqualTo(hearit1.getId());
        });
    }

    @Test
    @DisplayName("멤버 로그인한 사용자가 카테고리 내의 히어릿들을 조회 시 마지막 재생 시간도 함께 조회된다.")
    void getHearitsByCategory_lastPlayTime() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit1 = dbHelper.insertHearit(new Hearit(
                "title",
                "summary",
                500,
                "/hearit/audio/original/ORG_bf7c513e-579e-4224-8505-3824bb22ed01.mp3",
                "/hearit/audio/short/SHR_bf7c513e-579e-4224-8505-3824bb22ed01.mp3",
                "/hearit/script/SCR_bf7c513e-579e-4224-8505-3824bb22ed01.json",
                List.of(
                        new Source("이 컨텐츠는 쿠버네티스 공식 문서 (저작자: The Kubernetes Authors)를 참고하여 만들어졌습니다.",
                                "https://example.com/1"),
                        new Source("원본은 CC BY 4.0 라이선스를 따릅니다.", "https://example.com/2")
                ),
                category));
        Hearit hearit2 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Hearit hearit3 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        PlayingHistory playingHistory = dbHelper.insertPlayingHistory(new PlayingHistory(member.getId(), hearit1, 450));
        HearitSortRequest sortRequest = new HearitSortRequest(HearitSortField.CREATED_AT, SortDirection.DESC);
        PagingRequest pagingRequest = new PagingRequest(1, 2);

        // when
        PagedResponse<FilteredHearitResponse> result = hearitService.getFilteredHearits(category.getId(),
                sortRequest, TestFixture.createFixedMemberUserInfo(member), pagingRequest);

        // then
        assertAll(
                () -> {
                    assertThat(result.content()).hasSize(1);
                    assertThat(result.content().get(0).id()).isEqualTo(hearit1.getId());
                    assertThat(result.content().get(0).lastPlayTime()).isEqualTo(playingHistory.getLastPlayTime());
                }
        );
    }
}
