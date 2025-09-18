package com.onair.hearit.app.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.app.dto.request.PagingRequest;
import com.onair.hearit.app.dto.response.BookmarkHearitResponse;
import com.onair.hearit.app.dto.response.BookmarkInfoResponse;
import com.onair.hearit.auth.domain.RequestUser;
import com.onair.hearit.common.domain.Bookmark;
import com.onair.hearit.common.domain.Category;
import com.onair.hearit.common.domain.Hearit;
import com.onair.hearit.common.domain.Member;
import com.onair.hearit.common.domain.PlayingHistory;
import com.onair.hearit.common.domain.UserInfo;
import com.onair.hearit.common.exception.custom.AlreadyExistException;
import com.onair.hearit.common.exception.custom.ForbiddenException;
import com.onair.hearit.common.exception.custom.UnauthenticatedException;
import com.onair.hearit.common.infrastructure.jpa.BookmarkRepository;
import com.onair.hearit.common.infrastructure.jpa.HearitRepository;
import com.onair.hearit.common.infrastructure.jpa.MemberRepository;
import com.onair.hearit.common.infrastructure.jpa.PlayingHistoryRepository;
import com.onair.hearit.common.infrastructure.jpa.TestJpaAuditingConfig;
import com.onair.hearit.fixture.DbHelper;
import com.onair.hearit.fixture.TestFixture;
import java.util.List;
import java.util.UUID;
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
    @DisplayName("멤버 아이디에 따라 북마크한 히어릿 목록을 페이지에 따라 조회한다.")
    void getBookmarkHearitsTest() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        PlayingHistory playingHistory = dbHelper.insertPlayingHistory(new PlayingHistory(member.getId(), hearit, 1));

        Bookmark bookmark = dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit));

        // when
        List<BookmarkHearitResponse> responses = bookmarkService.getBookmarkHearits(
                RequestUser.member(member.getId()).getUserInfo(), new PagingRequest(0, 20)).content();

        // then
        assertAll(() -> {
            assertThat(responses).hasSize(1);
            assertThat(responses.getFirst().bookmarkId()).isEqualTo(bookmark.getId());
            assertThat(responses.getFirst().lastPlayTime()).isEqualTo(playingHistory.getLastPlayTime());
        });
    }

    @Test
    @DisplayName("로그인하지 않은 사용자는 북마크 목록 조회시 Unauthorized 예외를 던진다.")
    void getBookmarkHearitsTest_() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        UserInfo guestInfo = RequestUser.guest(UUID.randomUUID().toString()).getUserInfo();
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit));
        PagingRequest pagingRequest = new PagingRequest(0, 20);

        // when
        assertThatThrownBy(() -> bookmarkService.getBookmarkHearits(guestInfo, pagingRequest))
                .isInstanceOf(UnauthenticatedException.class);
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
        assertAll(() -> {
            assertThat(previousBookmarkCount + 1).isEqualTo(currentBookmarkCount);
            assertThat(response.id()).isNotNull();
        });
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
