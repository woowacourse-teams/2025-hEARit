package com.onair.hearit.core.infrastructure.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.core.domain.Bookmark;
import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.Member;
import com.onair.hearit.core.domain.PlayingHistory;
import com.onair.hearit.core.fixture.DbHelper;
import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import com.onair.hearit.core.infrastructure.projection.BookmarkWithPlayingHistoryProjection;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import({DbHelper.class, TestJpaAuditingConfig.class})
@ActiveProfiles("fake-test")
class BookmarkRepositoryTest {

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private DbHelper dbHelper;

    @Test
    @DisplayName("멤버의 북마크를 최신 순으로 조회한다.")
    void findAllByMemberOrderByRecentTest() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Hearit hearit2 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Hearit hearit3 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        Bookmark oldestBookmark = dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit1));
        Bookmark mideumBookmark = dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit2));
        Bookmark newestBookmark = dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit3));

        // when
        Page<BookmarkWithPlayingHistoryProjection> bookmarks = bookmarkRepository.findAllByMemberOrderByRecent(
                member.getId(),
                PageRequest.of(0, 5));

        // then
        assertAll(() -> {
            assertThat(bookmarks.getContent().get(0).getBookmark().getId()).isEqualTo(newestBookmark.getId());
            assertThat(bookmarks.getContent().get(1).getBookmark().getId()).isEqualTo(mideumBookmark.getId());
            assertThat(bookmarks.getContent().get(2).getBookmark().getId()).isEqualTo(oldestBookmark.getId());
        });
    }

    @Test
    @DisplayName("멤버의 북마크와 함께 재생 시간을 가져온다.")
    void findAllByMemberOrderByRecentWithPlayingTimeTest() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Bookmark bookmark = dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit));
        PlayingHistory playingHistory = dbHelper.insertPlayingHistory(new PlayingHistory(
                member.getId(),
                hearit,
                (hearit.getPlayTime() - 10) * 1000L));

        // when
        Page<BookmarkWithPlayingHistoryProjection> bookmarks = bookmarkRepository.findAllByMemberOrderByRecent(
                member.getId(),
                PageRequest.of(0, 5)
        );

        // then
        BookmarkWithPlayingHistoryProjection projection = bookmarks.getContent().get(0);

        assertAll(() -> {
            assertThat(bookmarks.getContent()).hasSize(1);
            assertThat(projection.getBookmark().getId()).isEqualTo(bookmark.getId());
            assertThat(projection.getPlayingHistory().getLastPlayTime()).isEqualTo(playingHistory.getLastPlayTime());
            assertThat(projection.getPlayingHistory().isFinished()).isTrue();
        });
    }

    @Test
    @DisplayName("멤버가 특정 카테고리에 북마크한 히어릿의 개수를 조회한다.")
    void countMemberBookmarksByCategoryIdTest() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Hearit hearit2 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Hearit hearit3 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit1));
        dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit2));
        dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit3));

        // when
        List<CategoryBookmarkCount> categoryBookmarkCounts = bookmarkRepository.countMemberBookmarksByCategoryId(
                member.getId());

        // then
        assertThat(categoryBookmarkCounts.getFirst().getCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("멤버의 북마크한 히어릿 중 청취 미완료인 목록을 조회한다. ")
    void findUnfinishedByMemberOrderByRecentTest() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit finished1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Hearit finished2 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Hearit unfinished1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Hearit unfinished2 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Hearit unfinished3 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, finished1));
        dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, finished2));
        dbHelper.insertPlayingHistory(new PlayingHistory(member.getId(), finished1, 500_000L));
        dbHelper.insertPlayingHistory(new PlayingHistory(member.getId(), finished2, 500_000L));

        dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, unfinished1));
        dbHelper.insertPlayingHistory(new PlayingHistory(member.getId(), unfinished1, 100L));
        dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, unfinished2));
        dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, unfinished3));

        // when
        var unfinishedBookmarks = bookmarkRepository.findUnfinishedByMemberOrderByRecent(
                member.getId(), PageRequest.of(0, 5));

        // then
        assertThat(unfinishedBookmarks.getContent()).hasSize(3);
    }
}
