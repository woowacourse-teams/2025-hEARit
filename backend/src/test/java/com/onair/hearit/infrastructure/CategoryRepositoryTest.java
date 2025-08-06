package com.onair.hearit.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.config.TestJpaAuditingConfig;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.Member;
import com.onair.hearit.fixture.DbHelper;
import com.onair.hearit.fixture.TestFixture;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import({DbHelper.class, TestJpaAuditingConfig.class})
@ActiveProfiles("fake-test")
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private DbHelper dbHelper;

    @Test
    @DisplayName("가장 오래된 순으로 지정된 개수만큼 카테고리를 조회한다.")
    void findOldest() {
        // given
        Category c1 = dbHelper.insertCategory(new Category("Java", "#FF0000"));
        Category c2 = dbHelper.insertCategory(new Category("Spring", "#00FF00"));
        Category c3 = dbHelper.insertCategory(new Category("React", "#0000FF"));

        // when
        List<Category> result = categoryRepository.findOldest(2);

        // then
        assertAll(
            () -> assertThat(result).hasSize(2),
            () -> assertThat(result.get(0).getId()).isEqualTo(c1.getId()),
            () -> assertThat(result.get(1).getId()).isEqualTo(c2.getId())
        );
    }

    @Test
    @DisplayName("가장 오래된 순으로 지정된 개수만큼 카테고리를 조회한다.")
    void findTopCategoriesByMemberBookmarks() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());

        Category c1 = dbHelper.insertCategory(new Category("Java", "#FF0000"));
        Category c2 = dbHelper.insertCategory(new Category("Spring", "#00FF00"));
        Category c3 = dbHelper.insertCategory(new Category("React", "#0000FF"));

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
        List<Category> result = categoryRepository.findTopCategoriesByMemberBookmarks(member.getId(), 3);

        // then
        assertAll(() -> {
            assertThat(result).hasSize(3);
            assertThat(result.get(0).getId()).isEqualTo(c1.getId());
            assertThat(result.get(1).getId()).isEqualTo(c2.getId());
            assertThat(result.get(2).getId()).isEqualTo(c3.getId());
        });
    }


}
