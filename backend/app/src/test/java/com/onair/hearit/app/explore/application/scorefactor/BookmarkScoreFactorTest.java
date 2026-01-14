package com.onair.hearit.explore.application.scorefactor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.onair.hearit.app.exception.custom.NotFoundException;
import com.onair.hearit.app.explore.application.scorefactor.BookmarkScoreFactor;
import com.onair.hearit.app.fixture.DbHelper;
import com.onair.hearit.core.domain.Bookmark;
import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.Member;
import com.onair.hearit.core.domain.UserType;
import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import com.onair.hearit.core.infrastructure.jpa.BookmarkRepository;
import com.onair.hearit.core.infrastructure.jpa.MemberRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import({DbHelper.class, TestJpaAuditingConfig.class})
@ActiveProfiles("fake-test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
class BookmarkScoreFactorTest {

    @Autowired
    private DbHelper dbHelper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    private BookmarkScoreFactor bookmarkScoreFactor;

    @BeforeEach
    void setup() {
        bookmarkScoreFactor = new BookmarkScoreFactor(memberRepository, bookmarkRepository);
    }

    @Test
    @DisplayName("MEMBER 타입은 지원하므로 true를 반환한다.")
    void isSupported_returnsTrueForMember() {
        // given
        UserType userType = UserType.MEMBER;

        // when
        boolean actual = bookmarkScoreFactor.isSupported(userType);

        // then
        assertThat(actual).isTrue();
    }

    @Test
    @DisplayName("GUEST 타입은 지원하지 않으므로 false를 반환한다.")
    void isSupported_returnsFalseForGuest() {
        // given
        UserType userType = UserType.GUEST;

        // when
        boolean actual = bookmarkScoreFactor.isSupported(userType);

        // then
        assertThat(actual).isFalse();
    }

    @Test
    @DisplayName("사용자의 카테고리별 북마크 비율에 비례하여 점수를 계산한다.")
    void basedOnBookmarkCounts() {
        // given
        Category itCategory = dbHelper.insertCategory(new Category("IT", "#112233"));
        Category javaCategory = dbHelper.insertCategory(new Category("Java", "#445566"));
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());

        // IT 카테고리 북마크 3개
        for (int i = 0; i < 3; i++) {
            Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(itCategory));
            dbHelper.insertBookmark(new Bookmark(member.getUuid(), hearit));
        }
        // Java 카테고리 북마크 1개
        Hearit javaHearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(javaCategory));
        dbHelper.insertBookmark(new Bookmark(member.getUuid(), javaHearit));
        // 총 북마크 4개

        // 점수 계산 대상 히어릿
        Hearit targetItHearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(itCategory));
        Hearit targetJavaHearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(javaCategory));
        List<Hearit> targets = List.of(targetItHearit, targetJavaHearit);

        // when
        Map<Long, Double> scores = bookmarkScoreFactor.calculate(member.getUuid(), targets);

        // then
        // IT 히어릿 점수: (3 / 4) * 30 = 22.5
        assertAll(
                () -> assertThat(scores.get(targetItHearit.getId())).isEqualTo(22.5),
                // Java 히어릿 점수: (1 / 4) * 30 = 7.5
                () -> assertThat(scores.get(targetJavaHearit.getId())).isEqualTo(7.5)
        );
    }

    @Test
    @DisplayName("사용자가 북마크하지 않은 카테고리의 히어릿은 0점을 받는다.")
    void UnbookmarkedCategories() {
        // given
        Category bookmarkedCategory = dbHelper.insertCategory(new Category("Bookmarked", "#112233"));
        Category unbookmarkedCategory = dbHelper.insertCategory(new Category("Unbookmarked", "#445566"));
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());

        // Bookmarked 카테고리에만 북마크 1개
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(bookmarkedCategory));
        dbHelper.insertBookmark(new Bookmark(member.getUuid(), hearit));

        Hearit target = dbHelper.insertHearit(TestFixture.createFixedHearitWith(unbookmarkedCategory));

        // when
        Map<Long, Double> scores = bookmarkScoreFactor.calculate(member.getUuid(), List.of(target));

        // then
        // 점수: (0 / 1) * 30 = 0.0
        assertThat(scores.get(target.getId())).isEqualTo(0.0);
    }

    @Test
    @DisplayName("사용자가 북마크를 하나도 하지 않았을 경우 모든 히어릿은 0점을 받는다.")
    void memberHasNoBookmarks() {
        // given
        Category category = dbHelper.insertCategory(new Category("any", "#112233"));
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());

        // when
        Map<Long, Double> scores = bookmarkScoreFactor.calculate(member.getUuid(), List.of(hearit));

        // then
        // 점수: (0 / 1) * 30 = 0.0
        assertThat(scores.get(hearit.getId())).isEqualTo(0.0);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 uuid로 요청 시 NotFoundException이 발생한다.")
    void memberUuidDoesNotExist() {
        // given
        UUID nonExistentUuid = UUID.randomUUID();
        Category category = dbHelper.insertCategory(new Category("any", "#112233"));
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        List<Hearit> hearits = List.of(hearit);

        // when
        // then
        assertThrows(NotFoundException.class,
                () -> bookmarkScoreFactor.calculate(nonExistentUuid, hearits));
    }
}
