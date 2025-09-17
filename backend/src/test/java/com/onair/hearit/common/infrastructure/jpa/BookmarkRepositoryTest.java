package com.onair.hearit.common.infrastructure.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.common.domain.Bookmark;
import com.onair.hearit.common.domain.Category;
import com.onair.hearit.common.domain.Hearit;
import com.onair.hearit.common.domain.Member;
import com.onair.hearit.common.infrastructure.dto.BookmarkWithPlaytimeProjection;
import com.onair.hearit.fixture.DbHelper;
import com.onair.hearit.fixture.TestFixture;
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
        Page<BookmarkWithPlaytimeProjection> bookmarks = bookmarkRepository.findAllByMemberOrderByRecent(
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
}
