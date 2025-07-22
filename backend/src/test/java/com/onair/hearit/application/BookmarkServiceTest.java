package com.onair.hearit.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.onair.hearit.DbHelper;
import com.onair.hearit.common.exception.custom.AlreadyExistException;
import com.onair.hearit.common.exception.custom.UnauthorizedException;
import com.onair.hearit.domain.Bookmark;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.Member;
import com.onair.hearit.dto.request.BookmarkListCondition;
import com.onair.hearit.dto.response.BookmarkHearitResponse;
import com.onair.hearit.infrastructure.BookmarkRepository;
import com.onair.hearit.infrastructure.HearitRepository;
import com.onair.hearit.infrastructure.MemberRepository;
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
class BookmarkServiceTest {

    @Autowired
    private DbHelper dbHelper;

    @Autowired
    private HearitRepository hearitRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    private BookmarkService bookmarkService;

    @BeforeEach
    void setup() {
        bookmarkService = new BookmarkService(hearitRepository, memberRepository, bookmarkRepository);
    }

    @Test
    @DisplayName("멤버 아이디에 따라 북마크한 히어릿 목록을 페이지에 따라 조회한다.")
    void getBookmarkHearitsTest() {
        // given
        Member member = saveMember();
        Hearit hearit = saveHearitWithSuffix(1);
        dbHelper.insertBookmark(new Bookmark(member, hearit));

        // when
        List<BookmarkHearitResponse> responses = bookmarkService.getBookmarkHearits(
                member.getId(), new BookmarkListCondition(0, 20));

        // then
        assertThat(responses).hasSize(1);
    }

    @Test
    @DisplayName("멤버 아이디와 히어릿 아이디로 북마크를 추가한다.")
    void addBookmarkTest() {
        // given
        Member member = saveMember();
        Hearit hearit = saveHearitWithSuffix(1);
        int previousBookmarkCount = bookmarkRepository.findAll().size();

        // when
        bookmarkService.addBookmark(hearit.getId(), member.getId());

        // then
        int currentBookmarkCount = bookmarkRepository.findAll().size();
        assertThat(previousBookmarkCount + 1).isEqualTo(currentBookmarkCount);
    }

    @Test
    @DisplayName("북마크 추가 시, 이미 북마크가 존재한다면 AlreadyExistException을 던진다.")
    void addBookmarkTest_AlreadyExistTest() {
        // given
        Member member = saveMember();
        Hearit hearit = saveHearitWithSuffix(1);
        dbHelper.insertBookmark(new Bookmark(member, hearit));

        // when & then
        assertThatThrownBy(() -> bookmarkService.addBookmark(hearit.getId(), member.getId()))
                .isInstanceOf(AlreadyExistException.class)
                .hasMessageContaining("이미 북마크된 히어릿입니다.");
    }

    @Test
    @DisplayName("북마크 아이디와 멤버 아이디로 북마크를 삭제한다.")
    void deleteBookmarkTest() {
        // given
        Member member = saveMember();
        Hearit hearit = saveHearitWithSuffix(1);
        Bookmark bookmark = dbHelper.insertBookmark(new Bookmark(member, hearit));

        // when
        bookmarkService.deleteBookmark(bookmark.getId(), member.getId());

        // then
        assertThat(bookmarkRepository.findById(bookmark.getId())).isNotPresent();
    }

    @Test
    @DisplayName("북마크 삭제 시, 북마크를 한 멤버가 아니라면 UnauthorizedException을 던진다.")
    void deleteBookmark_UnauthorizedTest() {
        // given
        Member bookmarkMember = saveMember();
        Member notBookmarkMember = saveMember();
        Hearit hearit = saveHearitWithSuffix(1);
        Bookmark bookmark = dbHelper.insertBookmark(new Bookmark(bookmarkMember, hearit));

        // when & then
        assertThatThrownBy(() -> bookmarkService.deleteBookmark(bookmark.getId(), notBookmarkMember.getId()))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("북마크를 삭제할 권한이 없습니다.");
    }

    private Member saveMember() {
        return dbHelper.insertMember(new Member("testId", "test1234!", null, "testMember"));
    }

    private Hearit saveHearitWithSuffix(int suffix) {
        Category category = new Category("name" + suffix, "#123");
        dbHelper.insertCategory(category);

        Hearit hearit = new Hearit(
                "title" + suffix,
                "summary" + suffix, suffix,
                "originalAudioUrl" + suffix,
                "shortAudioUrl" + suffix,
                "scriptUrl" + suffix,
                "source" + suffix,
                LocalDateTime.now(),
                category);
        return dbHelper.insertHearit(hearit);
    }
}
