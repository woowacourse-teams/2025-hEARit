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
    @DisplayName("회원의 북마크 수에 따라 상위 카테고리를 조회한다.")
    void findTopCategoriesByMemberBookmarks() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());

        Category category1 = dbHelper.insertCategory(new Category("Java", "#FF0000"));
        Category category2 = dbHelper.insertCategory(new Category("Spring", "#00FF00"));
        Category category3 = dbHelper.insertCategory(new Category("React", "#0000FF"));

        Hearit hearit11 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category1));
        Hearit hearit12 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category1));
        Hearit hearit13 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category1));
        Hearit hearit21 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category2));
        Hearit hearit22 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category2));
        Hearit hearit31 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category3));

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
            assertThat(result.get(0).getId()).isEqualTo(category1.getId());
            assertThat(result.get(1).getId()).isEqualTo(category2.getId());
            assertThat(result.get(2).getId()).isEqualTo(category3.getId());
        });
    }


}
