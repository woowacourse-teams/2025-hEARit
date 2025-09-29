package com.onair.hearit.explore.application.scorefactor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import com.onair.hearit.domain.Bookmark;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.Member;
import com.onair.hearit.fixture.DbHelper;
import com.onair.hearit.infrastructure.jpa.BookmarkRepository;
import com.onair.hearit.infrastructure.jpa.MemberRepository;
import java.util.List;
import java.util.Map;
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

    @DisplayName("사용자의 카테고리별 북마크 비율에 따라 비례하여 점수를 계산한다.")
    @Test
    void calculateBookmarkScores() {
        // given
        Category category1 = dbHelper.insertCategory(new Category("IT", "#112233"));
        Category category2 = dbHelper.insertCategory(new Category("Java", "#445566"));
        Category category3 = dbHelper.insertCategory(new Category("Android", "#778899"));
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());

        // 북마크용 히어릿
        Hearit hearit1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category1));
        Hearit hearit2 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category1));
        Hearit hearit3 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category1));
        Hearit hearit4 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category2));

        //  카테고리별 북마크 - category1 : 3개, category2: 1개
        dbHelper.insertBookmark(new Bookmark(member, hearit1));
        dbHelper.insertBookmark(new Bookmark(member, hearit2));
        dbHelper.insertBookmark(new Bookmark(member, hearit3));
        dbHelper.insertBookmark(new Bookmark(member, hearit4));

        // 점수 계산 대상이 될 히어릿들
        Hearit hearit5 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category1));
        Hearit hearit6 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category2));
        Hearit hearit7 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category3));

        List<Hearit> hearits = List.of(hearit5, hearit6, hearit7);

        // when
        Map<Long, Double> scores = bookmarkScoreFactor.calculate(member.getUuid(), hearits);

        // then
        assertAll(
                () -> assertThat(scores).hasSize(hearits.size()),
                //  hearit5(category1) 점수: (3 / 4) * 30 = 22.5
                () -> assertThat(scores.get(hearit5.getId())).isEqualTo(22.5),
                // hearit6(category2) 점수: (1 / 4) * 30 = 7.5
                () -> assertThat(scores.get(hearit6.getId())).isEqualTo(7.5),
                // hearit7(category3) 점수: (0 / 4) * 30 = 0.0
                () -> assertThat(scores.get(hearit7.getId())).isEqualTo(0.0)
        );
    }

    @DisplayName("북마크가 하나도 없는 경우 모든 히어릿의 점수는 0이다.")
    @Test
    void calculateBookmarkScoresWithoutAnyBookmarks() {
        // given
        Category category = dbHelper.insertCategory(new Category("red", "#112233"));
        Hearit hearit1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Hearit hearit2 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        List<Hearit> hearits = List.of(hearit1, hearit2);

        // when
        Map<Long, Double> scores = bookmarkScoreFactor.calculate(member.getUuid(), hearits);

        // then
        assertAll(
                () -> assertThat(scores).hasSize(hearits.size()),
                () -> assertThat(scores.values()).allMatch(score -> score == 0.0)
        );
    }
}
