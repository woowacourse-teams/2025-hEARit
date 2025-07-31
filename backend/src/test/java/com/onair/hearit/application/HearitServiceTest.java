package com.onair.hearit.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.common.exception.custom.NotFoundException;
import com.onair.hearit.config.TestJpaAuditingConfig;
import com.onair.hearit.domain.Bookmark;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.HearitKeyword;
import com.onair.hearit.domain.Keyword;
import com.onair.hearit.domain.Member;
import com.onair.hearit.dto.request.PagingRequest;
import com.onair.hearit.dto.response.GroupedHearitsWithCategoryResponse;
import com.onair.hearit.dto.response.HearitDetailResponse;
import com.onair.hearit.dto.response.PagedResponse;
import com.onair.hearit.dto.response.RandomHearitResponse;
import com.onair.hearit.dto.response.RecommendHearitResponse;
import com.onair.hearit.fixture.DbHelper;
import com.onair.hearit.fixture.TestFixture;
import com.onair.hearit.infrastructure.BookmarkRepository;
import com.onair.hearit.infrastructure.CategoryRepository;
import com.onair.hearit.infrastructure.HearitKeywordRepository;
import com.onair.hearit.infrastructure.HearitRepository;
import java.util.List;
import java.util.stream.IntStream;
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

    private HearitService hearitService;

    @BeforeEach
    void setup() {
        hearitService = new HearitService(
                hearitRepository,
                bookmarkRepository,
                hearitKeywordRepository,
                categoryRepository);
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
        HearitDetailResponse response = hearitService.getHearitDetail(hearit.getId(), member.getId());

        // then
        assertAll(
                () -> assertThat(response.id()).isEqualTo(hearit.getId()),
                () -> assertThat(response.title()).isEqualTo(hearit.getTitle()),
                () -> assertThat(response.summary()).isEqualTo(hearit.getSummary()),
                () -> assertThat(response.isBookmarked()).isTrue(),
                () -> assertThat(response.bookmarkId()).isEqualTo(bookmark.getId()),
                () -> assertThat(response.category()).isEqualTo(hearit.getCategory().getName()),
                () -> assertThat(response.keywords()).hasSize(1)
        );
    }

    @Test
    @DisplayName("존재하지 않는 히어릿 아이디로 단일 히어릿 조회 시 NoFoundException을 던진다.")
    void getHearitDetailNotFoundTest() {
        // given
        Long notExistHearitId = 1L;
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());

        // when & then
        assertThatThrownBy(() -> hearitService.getHearitDetail(notExistHearitId, member.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("hearitId");
    }

    @Test
    @DisplayName("최대 10개의 랜덤 히어릿을 조회할 수 있다.")
    void getRandomHearits() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());

        for (int i = 1; i <= 11; i++) {
            Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
            dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit));
        }
        PagingRequest pagingRequest = new PagingRequest(0, 10);

        // when
        PagedResponse<RandomHearitResponse> hearits = hearitService.getRandomHearits(member.getId(), pagingRequest);

        // then
        assertThat(hearits.content()).hasSize(10);
    }

    @Test
    @DisplayName("최대 5개의 추천 히어릿을 조회할 수 있다.")
    void getRecommendedHearits() {
        // given
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        IntStream.rangeClosed(1, 6)
                .forEach((num) -> dbHelper.insertHearit(TestFixture.createFixedHearitWith(category)));

        // when
        List<RecommendHearitResponse> hearits = hearitService.getRecommendedHearits();

        // then
        assertThat(hearits).hasSize(5);
    }

    @Test
    @DisplayName("홈 카테고리의 히어릿들을 키워드와 함께 조회할 수 있다.")
    void getHomeCategoryHearits() {
        // given
        Category category1 = dbHelper.insertCategory(TestFixture.createFixedCategory());
        dbHelper.insertHearit(TestFixture.createFixedHearitWith(category1));
        dbHelper.insertHearit(TestFixture.createFixedHearitWith(category1));
        dbHelper.insertHearit(TestFixture.createFixedHearitWith(category1));
        Category category2 = dbHelper.insertCategory(TestFixture.createFixedCategory());
        dbHelper.insertHearit(TestFixture.createFixedHearitWith(category2));
        dbHelper.insertHearit(TestFixture.createFixedHearitWith(category2));
        dbHelper.insertHearit(TestFixture.createFixedHearitWith(category2));
        Category category3 = dbHelper.insertCategory(TestFixture.createFixedCategory());
        dbHelper.insertHearit(TestFixture.createFixedHearitWith(category3));
        dbHelper.insertHearit(TestFixture.createFixedHearitWith(category3));
        dbHelper.insertHearit(TestFixture.createFixedHearitWith(category3));

        // when
        List<GroupedHearitsWithCategoryResponse> responses = hearitService.getHomeCategoryHearits();

        // then
        assertAll(
                () -> assertThat(responses).hasSize(3),
                () -> assertThat(responses.get(0).hearits()).hasSize(3),
                () -> assertThat(responses.get(1).hearits()).hasSize(3),
                () -> assertThat(responses.get(2).hearits()).hasSize(3)
        );
    }
}
