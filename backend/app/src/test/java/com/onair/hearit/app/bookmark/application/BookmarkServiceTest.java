package com.onair.hearit.app.bookmark.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.app.auth.domain.RequestUser;
import com.onair.hearit.app.bookmark.dto.BookmarkHearitResponseV2;
import com.onair.hearit.app.bookmark.dto.BookmarkInfoResponse;
import com.onair.hearit.app.bookmark.dto.param.BookmarkFilter;
import com.onair.hearit.app.bookmark.dto.param.BookmarkSort;
import com.onair.hearit.app.bookmark.dto.param.BookmarkSort.BookmarkSortDirection;
import com.onair.hearit.app.bookmark.dto.param.BookmarkSort.BookmarkSortType;
import com.onair.hearit.app.common.dto.request.PagingRequest;
import com.onair.hearit.app.common.dto.response.PagedResponse;
import com.onair.hearit.app.exception.custom.AlreadyExistException;
import com.onair.hearit.app.exception.custom.ForbiddenException;
import com.onair.hearit.app.fixture.DbHelper;
import com.onair.hearit.core.domain.Bookmark;
import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.Member;
import com.onair.hearit.core.domain.PlayingHistory;
import com.onair.hearit.core.domain.UserInfo;
import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import com.onair.hearit.core.infrastructure.jpa.BookmarkRepository;
import com.onair.hearit.core.infrastructure.jpa.HearitRepository;
import com.onair.hearit.core.infrastructure.jpa.MemberRepository;
import com.onair.hearit.core.infrastructure.jpa.PlayingHistoryRepository;
import java.util.List;
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
class BookmarkServiceTest {

    @Autowired
    private DbHelper dbHelper;

    @Autowired
    private HearitRepository hearitRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private PlayingHistoryRepository playingHistoryRepository;

    private BookmarkService bookmarkService;

    @BeforeEach
    void setup() {
        bookmarkService = new BookmarkService(hearitRepository, memberRepository, bookmarkRepository);
    }

    @Test
    @DisplayName("북마크 조회 시 비회원인 경우 빈 값을 반환한다.")
    void getBookmarkHearits_isNotMemberGuest() {
        // given
        RequestUser guest = RequestUser.guest("00000000-0000-0000-0000-000000000000");

        // when
        PagedResponse<BookmarkHearitResponseV2> bookmarkHearits = bookmarkService.getBookmarkHearits(
                guest.getUserInfo(),
                new PagingRequest(0, 20),
                BookmarkFilter.ALL,
                new BookmarkSort(BookmarkSortType.CREATED_AT, BookmarkSortDirection.DESC));

        // then
        assertAll(
                () -> assertThat(bookmarkHearits.size()).isZero(),
                () -> assertThat(bookmarkHearits.content()).isEmpty());
    }


    @Test
    @DisplayName("멤버가 북마크한 전체 히어릿 목록을 페이지에 따라 조회한다.")
    void getAllBookmarkHearitsTest() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        PlayingHistory playingHistory = dbHelper.insertPlayingHistory(new PlayingHistory(member.getId(), hearit, 1));

        Bookmark bookmark = dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit));

        // when
        List<BookmarkHearitResponseV2> responses = bookmarkService.getBookmarkHearits(
                        RequestUser.member(member.getId()).getUserInfo(),
                        new PagingRequest(0, 20),
                        BookmarkFilter.ALL,
                        new BookmarkSort(BookmarkSortType.CREATED_AT, BookmarkSortDirection.DESC))
                .content();

        // then
        assertAll(
                () -> assertThat(responses).hasSize(1),
                () -> assertThat(responses.getFirst().bookmarkId()).isEqualTo(bookmark.getId()),
                () -> assertThat(responses.getFirst().lastPlayTime()).isEqualTo(playingHistory.getLastPlayTime()));
    }

    @Test
    @DisplayName("멤버가 북마크한 청취 미완료 히어릿 목록을 페이지에 따라 조회한다.")
    void getUnfinishedBookmarkHearitsTest() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());

        Hearit finished = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Bookmark bookmark1 = dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, finished));
        PlayingHistory playingHistory = dbHelper.insertPlayingHistory(
                new PlayingHistory(member.getId(), finished, 500_000L));

        Hearit unfinished = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Bookmark bookmark2 = dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, unfinished));

        // when
        List<BookmarkHearitResponseV2> responses = bookmarkService.getBookmarkHearits(
                        RequestUser.member(member.getId()).getUserInfo(),
                        new PagingRequest(0, 20),
                        BookmarkFilter.UNFINISHED,
                        new BookmarkSort(BookmarkSortType.CREATED_AT, BookmarkSortDirection.DESC))
                .content();

        // then
        assertAll(
                () -> assertThat(responses).hasSize(1),
                () -> assertThat(responses.getFirst().bookmarkId()).isEqualTo(bookmark2.getId()),
                () -> assertThat(responses.getFirst().hearitId()).isEqualTo(unfinished.getId())
        );
    }

    @Test
    @DisplayName("멤버 아이디와 히어릿 아이디로 북마크를 추가한다.")
    void addBookmarkTest() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        int previousBookmarkCount = bookmarkRepository.findAll().size();

        // when
        BookmarkInfoResponse response = bookmarkService.addBookmark(RequestUser.member(member.getId()).getUserInfo(),
                hearit.getId());

        // then
        int currentBookmarkCount = bookmarkRepository.findAll().size();
        assertAll(
                () -> assertThat(previousBookmarkCount + 1).isEqualTo(currentBookmarkCount),
                () -> assertThat(response.id()).isNotNull()
        );
    }

    @Test
    @DisplayName("북마크 추가 시, 이미 북마크가 존재한다면 AlreadyExistException을 던진다.")
    void addBookmarkTest_AlreadyExistTest() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        UserInfo memberInfo = RequestUser.member(member.getId()).getUserInfo();
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Long hearitId = hearit.getId();

        dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit));

        // when & then
        assertThatThrownBy(() -> bookmarkService.addBookmark(memberInfo, hearitId))
                .isInstanceOf(AlreadyExistException.class)
                .hasMessageContaining("이미 북마크된 히어릿입니다.");
    }

    @Test
    @DisplayName("북마크 아이디와 멤버 아이디로 북마크를 삭제한다.")
    void deleteBookmarkTest() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        Bookmark bookmark = dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit));

        // when
        bookmarkService.deleteBookmark(bookmark.getId(), RequestUser.member(member.getId()).getUserInfo());

        // then
        assertThat(bookmarkRepository.findById(bookmark.getId())).isNotPresent();
    }

    @Test
    @DisplayName("북마크 삭제 시, 북마크를 한 멤버가 아니라면 Forbidden을 던진다.")
    void deleteBookmark_UnauthorizedTest() {
        // given
        Member bookmarkMember = dbHelper.insertMember(TestFixture.createFixedMember());
        Member notBookmarkMember = dbHelper.insertMember(TestFixture.createFixedMember());
        UserInfo notBookmarkMemberInfo = RequestUser.member(notBookmarkMember.getId()).getUserInfo();

        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());

        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Long bookmarkId = dbHelper.insertBookmark(TestFixture.createFixedBookmark(bookmarkMember, hearit)).getId();

        // when & then
        assertThatThrownBy(
                () -> bookmarkService.deleteBookmark(bookmarkId, notBookmarkMemberInfo))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("북마크를 삭제할 권한이 없습니다.");
    }
}
