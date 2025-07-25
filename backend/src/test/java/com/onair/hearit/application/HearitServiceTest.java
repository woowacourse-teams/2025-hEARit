package com.onair.hearit.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.common.exception.custom.NotFoundException;
import com.onair.hearit.config.TestJpaAuditingConfig;
import com.onair.hearit.domain.Bookmark;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.Member;
import com.onair.hearit.dto.request.PagingRequest;
import com.onair.hearit.dto.response.HearitDetailResponse;
import com.onair.hearit.dto.response.PagedResponse;
import com.onair.hearit.dto.response.RandomHearitResponse;
import com.onair.hearit.dto.response.RecommendHearitResponse;
import com.onair.hearit.fixture.DbHelper;
import com.onair.hearit.fixture.TestFixture;
import com.onair.hearit.infrastructure.BookmarkRepository;
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

    private HearitService hearitService;

    @BeforeEach
    void setup() {
        hearitService = new HearitService(hearitRepository, bookmarkRepository);
    }

    @Test
    @DisplayName("히어릿 아이디로 단일 히어릿 정보를 조회 할 수 있다.")
    void getHearitDetailTest() {
        // given
        Member member = saveMember();
        Category category = saveCategory();
        Hearit hearit = saveHearit(category);
        Bookmark bookmark = saveBookmark(member, hearit);

        // when
        HearitDetailResponse response = hearitService.getHearitDetail(hearit.getId(), member.getId());

        // then
        assertAll(
                () -> assertThat(response.id()).isEqualTo(hearit.getId()),
                () -> assertThat(response.title()).isEqualTo(hearit.getTitle()),
                () -> assertThat(response.summary()).isEqualTo(hearit.getSummary()),
                () -> assertThat(response.isBookmarked()).isTrue(),
                () -> assertThat(response.bookmarkId()).isEqualTo(bookmark.getId())
        );
    }

    @Test
    @DisplayName("존재하지 않는 히어릿 아이디로 단일 히어릿 조회 시 NoFoundException을 던진다.")
    void getHearitDetailNotFoundTest() {
        // given
        Long notExistHearitId = 1L;
        Member member = saveMember();

        // when & then
        assertThatThrownBy(() -> hearitService.getHearitDetail(notExistHearitId, member.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("hearitId");
    }

    @Test
    @DisplayName("최대 10개의 랜덤 히어릿을 조회할 수 있다.")
    void getRandomHearits() {
        // given
        Member member = saveMember();
        Category category = saveCategory();

        for (int i = 1; i <= 11; i++) {
            Hearit hearit = saveHearit(category);
            saveBookmark(member, hearit);
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
        Category category = saveCategory();
        IntStream.rangeClosed(1, 6)
                .forEach((num) -> saveHearit(category));

        // when
        List<RecommendHearitResponse> hearits = hearitService.getRecommendedHearits();

        // then
        assertThat(hearits).hasSize(5);
    }

    private Member saveMember() {
        return dbHelper.insertMember(TestFixture.createFixedMember());
    }

    private Category saveCategory() {
        return dbHelper.insertCategory(TestFixture.createFixedCategory());
    }

    private Hearit saveHearit(Category category) {
        return dbHelper.insertHearit(TestFixture.createFixedHearit(category));
    }

    private Bookmark saveBookmark(Member member, Hearit hearit) {
        return dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit));
    }
}
