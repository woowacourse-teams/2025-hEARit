package com.onair.hearit.app.category.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.app.exception.custom.NotFoundException;
import com.onair.hearit.app.fixture.DbHelper;
import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.Member;
import com.onair.hearit.core.domain.UserInfo;
import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import({DbHelper.class, TestJpaAuditingConfig.class, RecommendCategoryService.class})
@ActiveProfiles("fake-test")
class RecommendCategoryServiceTest {

    @Autowired
    DbHelper dbHelper;

    @Autowired
    RecommendCategoryService recommendCategoryService;

    @Nested
    @DisplayName("게스트 사용자(비로그인)의 추천 카테고리 조회 시")
    class Guest_User {

        @Test
        @DisplayName("IT 트렌드 1개와 랜덤 카테고리 4개를 포함하여 총 5개의 카테고리를 반환한다")
        void guest_returns_itTrend_and_random() {
            // given
            UserInfo userInfo = new UserInfo(null, UUID.randomUUID().toString());

            Category c1 = dbHelper.insertCategory(TestFixture.createFixedCategory());
            Category c2 = dbHelper.insertCategory(TestFixture.createFixedCategory());
            Category c3 = dbHelper.insertCategory(TestFixture.createFixedCategory());
            Category c4 = dbHelper.insertCategory(TestFixture.createFixedCategory());
            Category c5 = dbHelper.insertCategory(TestFixture.createFixedCategory());
            Category itTrendCategory = dbHelper.insertCategory(TestFixture.createCategoryByName("IT 트렌드"));

            // when
            List<Category> recommendedCategories = recommendCategoryService.getRecommendedCategoriesFor(userInfo);

            // then
            assertAll(
                    () -> assertThat(recommendedCategories).hasSize(5),
                    () -> assertThat(recommendedCategories.get(0).getId()).isEqualTo(itTrendCategory.getId())
            );
        }
    }

    @Nested
    @DisplayName("로그인한 사용자의 추천 카테고리 조회 시")
    class Member_User {

        @Test
        @DisplayName("북마크한 카테고리가 3개 이상이면 IT 트렌드 1개, 사용자 추천 3개, 랜덤 1개를 반환한다")
        void member_with_many_bookmarks() {
            // given
            Member member = dbHelper.insertMember(TestFixture.createFixedMember());

            Category c1 = dbHelper.insertCategory(TestFixture.createFixedCategory());
            Category c2 = dbHelper.insertCategory(TestFixture.createFixedCategory());
            Category c3 = dbHelper.insertCategory(TestFixture.createFixedCategory());
            Category c4 = dbHelper.insertCategory(TestFixture.createFixedCategory());
            Category c5 = dbHelper.insertCategory(TestFixture.createFixedCategory());
            Category itTrendCategory = dbHelper.insertCategory(TestFixture.createCategoryByName("IT 트렌드"));

            Hearit hearit1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(c1));
            Hearit hearit2 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(c1));
            Hearit hearit3 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(c1));
            Hearit hearit4 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(c2));
            Hearit hearit5 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(c2));
            Hearit hearit6 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(c3));

            //c1에 북마크 3, c2에 북마크 2, c3에 북마크 1
            dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit1));
            dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit2));
            dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit3));
            dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit4));
            dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit5));
            dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit6));

            UserInfo userInfo = new UserInfo(member.getId(), null);

            // when
            List<Category> recommendedCategories = recommendCategoryService.getRecommendedCategoriesFor(userInfo);

            // then
            List<Long> recommendedIds = recommendedCategories.stream()
                    .map(Category::getId)
                    .toList();
            List<Long> expectedRandomIds = List.of(c4.getId(), c5.getId());
            assertAll(
                    () -> assertThat(recommendedCategories).hasSize(5),
                    () -> assertThat(recommendedIds.get(0)).isEqualTo(itTrendCategory.getId()),
                    () -> assertThat(recommendedIds.get(1)).isEqualTo(c1.getId()),
                    () -> assertThat(recommendedIds.get(2)).isEqualTo(c2.getId()),
                    () -> assertThat(recommendedIds.get(3)).isEqualTo(c3.getId()),
                    () -> assertThat(recommendedIds.get(4)).isIn(expectedRandomIds)
            );
        }

        @Test
        @DisplayName("북마크한 카테고리가 1개이면 IT 트렌드 1개, 사용자 추천 1개, 랜덤 3개를 반환한다")
        void member_with_one_bookmark() {
            Member member = dbHelper.insertMember(TestFixture.createFixedMember());

            Category c1 = dbHelper.insertCategory(TestFixture.createFixedCategory());
            Category c2 = dbHelper.insertCategory(TestFixture.createFixedCategory());
            Category c3 = dbHelper.insertCategory(TestFixture.createFixedCategory());
            Category c4 = dbHelper.insertCategory(TestFixture.createFixedCategory());
            Category c5 = dbHelper.insertCategory(TestFixture.createFixedCategory());
            Category itTrendCategory = dbHelper.insertCategory(TestFixture.createCategoryByName("IT 트렌드"));

            Hearit hearit1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(c1));
            dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit1));

            UserInfo userInfo = new UserInfo(member.getId(), null);

            // when
            List<Category> recommendedCategories = recommendCategoryService.getRecommendedCategoriesFor(userInfo);

            // then
            List<Long> recommendedIds = recommendedCategories.stream()
                    .map(Category::getId)
                    .toList();

            List<Long> expectedRandomIds = List.of(c2.getId(), c3.getId(), c4.getId(), c5.getId());
            assertAll(
                    () -> assertThat(recommendedCategories).hasSize(5),
                    () -> assertThat(recommendedIds.get(0)).isEqualTo(itTrendCategory.getId()),
                    () -> assertThat(recommendedIds.get(1)).isEqualTo(c1.getId()),
                    () -> assertThat(recommendedIds.subList(2, 5)).isSubsetOf(expectedRandomIds),
                    () -> assertThat(recommendedIds.subList(2, 5)).doesNotHaveDuplicates()
            );
        }

        @Test
        @DisplayName("북마크한 카테고리가 없으면 IT 트렌드 1개, 랜덤 4개를 반환한다")
        void member_without_bookmarks() {
            Member member = dbHelper.insertMember(TestFixture.createFixedMember());

            Category c1 = dbHelper.insertCategory(TestFixture.createFixedCategory());
            Category c2 = dbHelper.insertCategory(TestFixture.createFixedCategory());
            Category c3 = dbHelper.insertCategory(TestFixture.createFixedCategory());
            Category c4 = dbHelper.insertCategory(TestFixture.createFixedCategory());
            Category c5 = dbHelper.insertCategory(TestFixture.createFixedCategory());
            Category itTrendCategory = dbHelper.insertCategory(TestFixture.createCategoryByName("IT 트렌드"));

            UserInfo userInfo = new UserInfo(member.getId(), null);

            // when
            List<Category> recommendedCategories = recommendCategoryService.getRecommendedCategoriesFor(userInfo);

            // then
            List<Long> recommendedIds = recommendedCategories.stream()
                    .map(Category::getId)
                    .toList();

            List<Long> expectedRandomIds = List.of(c1.getId(), c2.getId(), c3.getId(), c4.getId(), c5.getId());
            assertAll(
                    () -> assertThat(recommendedCategories).hasSize(5),
                    () -> assertThat(recommendedIds.get(0)).isEqualTo(itTrendCategory.getId()),
                    () -> assertThat(recommendedIds.subList(1, 5)).isSubsetOf(expectedRandomIds),
                    () -> assertThat(recommendedIds.subList(1, 5)).doesNotHaveDuplicates()
            );
        }
    }

    @Nested
    @DisplayName("예외 및 특수 상황 처리 테스트")
    class Context_With_Edge_Cases {

        @Test
        @DisplayName("DB에 'IT 트렌드' 카테고리가 없으면 NotFoundException이 발생한다")
        void missing_itTrend_category() {
            // given
            dbHelper.insertCategory(TestFixture.createFixedCategory());
            dbHelper.insertCategory(TestFixture.createFixedCategory());
            UserInfo guest = new UserInfo(null, UUID.randomUUID().toString());

            // when & then
            assertThatThrownBy(() -> recommendCategoryService.getRecommendedCategoriesFor(guest))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("IT 트렌드");
        }

        @Test
        @DisplayName("UserInfo의 memberId에 해당하는 사용자가 없으면 NotFoundException이 발생한다")
        void missing_member_throws_exception() {
            // given
            dbHelper.insertCategory(TestFixture.createCategoryByName("IT 트렌드"));
            long nonExistId = 999L;
            UserInfo userInfo = new UserInfo(nonExistId, null);

            // when & then
            assertThatThrownBy(() -> recommendCategoryService.getRecommendedCategoriesFor(userInfo))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("memberId");
        }

        @Test
        @DisplayName("전체 카테고리 수가 5개 미만일 경우, 모든 카테고리를 반환한다")
        void less_than_total_categories() {
            // given
            Category c1 = dbHelper.insertCategory(TestFixture.createFixedCategory());
            Category c2 = dbHelper.insertCategory(TestFixture.createFixedCategory());
            Category c3 = dbHelper.insertCategory(TestFixture.createFixedCategory());
            Category itTrendCategory = dbHelper.insertCategory(TestFixture.createCategoryByName("IT 트렌드"));
            UserInfo guest = new UserInfo(null, UUID.randomUUID().toString());

            // when
            List<Category> recommended = recommendCategoryService.getRecommendedCategoriesFor(guest);

            // then
            List<Long> ids = recommended.stream().map(Category::getId).toList();
            assertAll(
                    () -> assertThat(recommended).hasSize(4),
                    () -> assertThat(ids).containsExactlyInAnyOrder(
                            itTrendCategory.getId(),
                            c1.getId(),
                            c2.getId(),
                            c3.getId()
                    ),
                    () -> assertThat(ids).doesNotHaveDuplicates()
            );
        }
    }
}
