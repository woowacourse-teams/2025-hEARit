package com.onair.hearit.app.recommendation.application;

import com.onair.hearit.app.fixture.DbHelper;
import com.onair.hearit.app.recommendation.dto.RecommendationByCategoryResponse;
import com.onair.hearit.app.recommendation.dto.RecommendationByCategoryResponse.HearitResponse;
import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.Member;
import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import({DbHelper.class, TestJpaAuditingConfig.class,
        CategoryRecommender.class, RecommendationService.class})
@ActiveProfiles("fake-test")
class RecommendationServiceTest {

    @Autowired
    DbHelper dbHelper;

    @Autowired
    RecommendationService recommendationService;

    @Test
    @DisplayName("추천카테고리별 히어릿들을 요청한 만큼 반환한다")
    void getHearitsWithRecommendCategory() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());

        Category itTrend = dbHelper.insertCategory(TestFixture.createCategoryByName("IT 트렌드"));
        Category categoryA = dbHelper.insertCategory(TestFixture.createCategoryByName("Category A"));
        Category categoryB = dbHelper.insertCategory(TestFixture.createCategoryByName("Category B"));
        Category categoryC = dbHelper.insertCategory(TestFixture.createCategoryByName("Category C"));

        for (int i = 0; i < 3; i++) {
            dbHelper.insertHearit(TestFixture.createFixedHearitWith(categoryA));
            dbHelper.insertHearit(TestFixture.createFixedHearitWith(categoryB));
        }
        Hearit hearit_it1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(itTrend));
        Hearit hearit_it2 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(itTrend));

        // when
        int categorySize = 3;
        int hearitSize = 2;
        List<RecommendationByCategoryResponse> responses = recommendationService.getCategoryRecommendations(
                TestFixture.createFixedMemberUserInfo(member),categorySize, hearitSize);

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(responses).hasSize(3);
            softly.assertThat(responses)
                    .extracting("categoryName", String.class)
                    .allMatch(name -> List.of("IT 트렌드", "Category A", "Category B", "Category C").contains(name));

            RecommendationByCategoryResponse itTrendResponse = responses.stream()
                    .filter(r -> r.categoryName().equals("IT 트렌드"))
                    .findFirst()
                    .orElseThrow();
            softly.assertThat(itTrendResponse.hearits()).extracting(HearitResponse::hearitId)
                    .containsExactlyInAnyOrder(hearit_it1.getId(), hearit_it2.getId());
        });
    }
}
