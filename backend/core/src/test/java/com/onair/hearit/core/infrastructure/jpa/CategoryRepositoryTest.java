package com.onair.hearit.core.infrastructure.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.Member;
import com.onair.hearit.core.fixture.DbHelper;
import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import java.util.Collections;
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
    @DisplayName("회원의 북마크 수에 따라 상위 카테고리를 조회한다. (제외 카테고리 없음)")
    void findTopCategoriesByMemberBookmarks_withoutExcludedIds() {
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
        List<Category> result = categoryRepository.findTopCategoriesByMemberBookmarks(
                member.getId(),
                3,
                Collections.emptyList()
        );

        // then
        assertAll(() -> {
            assertThat(result).hasSize(3);
            assertThat(result.get(0).getId()).isEqualTo(category1.getId()); // 3개
            assertThat(result.get(1).getId()).isEqualTo(category2.getId()); // 2개
            assertThat(result.get(2).getId()).isEqualTo(category3.getId()); // 1개
        });
    }

    @Test
    @DisplayName("제외 카테고리를 지정하면 해당 카테고리는 조회되지 않는다.")
    void findTopCategoriesByMemberBookmarks_withExcludedIds() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());

        Category category1 = dbHelper.insertCategory(new Category("Java", "#FF0000"));
        Category category2 = dbHelper.insertCategory(new Category("Spring", "#00FF00"));
        Category category3 = dbHelper.insertCategory(new Category("React", "#0000FF"));

        Hearit hearit11 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category1));
        Hearit hearit12 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category1));
        Hearit hearit21 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category2));
        Hearit hearit31 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category3));

        dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit11));
        dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit12));
        dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit21));
        dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit31));

        // when: category1 제외
        List<Category> result = categoryRepository.findTopCategoriesByMemberBookmarks(
                member.getId(),
                3,
                List.of(category1.getId())
        );

        // then
        assertAll(() -> {
            assertThat(result).hasSize(2);
            assertThat(result).extracting(Category::getId)
                    .containsExactly(category2.getId(), category3.getId());
        });
    }

    @Test
    @DisplayName("제외 ID 목록을 뺀 카테고리 ID만 조회한다.")
    void findIdsWithoutExcludedIds() {
        // given
        Category category1 = dbHelper.insertCategory(new Category("Java", "#FF0000"));
        Category category2 = dbHelper.insertCategory(new Category("Spring", "#00FF00"));
        Category category3 = dbHelper.insertCategory(new Category("React", "#0000FF"));

        // when
        List<Long> result = categoryRepository.findIdsWithoutExcludedIds(List.of(category2.getId()));

        // then
        assertThat(result).containsExactlyInAnyOrder(category1.getId(), category3.getId());
    }
}
