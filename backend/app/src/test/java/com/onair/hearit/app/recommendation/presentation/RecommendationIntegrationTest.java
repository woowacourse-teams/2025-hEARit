package com.onair.hearit.app.recommendation.presentation;

import com.onair.hearit.app.auth.infrastructure.jwt.JwtTokenProvider;
import com.onair.hearit.app.fixture.IntegrationTest;
import com.onair.hearit.app.recommendation.dto.RecommendationByCategoryResponse;
import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.Member;
import com.onair.hearit.core.fixture.TestFixture;
import io.restassured.RestAssured;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

class RecommendationIntegrationTest extends IntegrationTest {

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("(회원 - token O) 요청한 카테고리·히어릿 개수에 맞춰 추천 카테고리별 히어릿을 반환한다.")
    void readRecommendationsByCategory_member() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        String token = generateToken(member);

        Category category1 = dbHelper.insertCategory(new Category("Java", "#FF0000"));
        Category category2 = dbHelper.insertCategory(new Category("Spring", "#00FF00"));
        Category category3 = dbHelper.insertCategory(new Category("React1", "#0000FF"));
        Category category4 = dbHelper.insertCategory(new Category("React2", "#0000FF"));
        Category category5 = dbHelper.insertCategory(new Category("React3", "#0000FF"));
        Category category6 = dbHelper.insertCategory(new Category("React4", "#0000FF"));
        Category itTrendCategory = dbHelper.insertCategory(new Category("IT 트렌드", "#0000FF"));

        for (int i = 0; i < 5; i++) {
            dbHelper.insertHearit(TestFixture.createFixedHearitWith(category1));
            dbHelper.insertHearit(TestFixture.createFixedHearitWith(category2));
            dbHelper.insertHearit(TestFixture.createFixedHearitWith(category3));
            dbHelper.insertHearit(TestFixture.createFixedHearitWith(category4));
            dbHelper.insertHearit(TestFixture.createFixedHearitWith(category5));
            dbHelper.insertHearit(TestFixture.createFixedHearitWith(category6));
            dbHelper.insertHearit(TestFixture.createFixedHearitWith(itTrendCategory));
        }

        //북마크용 히어릿 + 북마크
        Hearit hearit1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category1));
        Hearit hearit2 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category1));
        Hearit hearit3 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category1));
        Hearit hearit4 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category2));
        Hearit hearit5 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category2));
        Hearit hearit6 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category3));
        dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit1));
        dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit2));
        dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit3));
        dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit4));
        dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit5));
        dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit6));

        // when
        int categorySize = 5;
        int hearitSize = 5;
        List<RecommendationByCategoryResponse> responses = RestAssured.given(this.spec)
                .header("Authorization", "Bearer " + token)
                .queryParam("categorySize", categorySize)
                .queryParam("hearitSize", hearitSize)
                .when()
                .get("/api/v1/recommendations/categories")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getList(".", RecommendationByCategoryResponse.class);

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(responses).hasSize(categorySize);

            RecommendationByCategoryResponse exampleSingleResponse = responses.get(0);
            softly.assertThat(exampleSingleResponse.hearits()).hasSize(hearitSize);
            softly.assertThat(exampleSingleResponse.hearits().get(0).hearitId()).isNotNull();
            softly.assertThat(exampleSingleResponse.categoryId()).isNotNull();
        });
    }

    @Test
    @DisplayName("(비회원 - token X)요청한 카테고리·히어릿 개수에 맞춰 추천 카테고리별 히어릿을 반환한다.")
    void readRecommendationsByCategory_guest() {
        // given
        Category category1 = dbHelper.insertCategory(new Category("Java", "#FF0000"));
        Category category2 = dbHelper.insertCategory(new Category("Spring", "#00FF00"));
        Category category3 = dbHelper.insertCategory(new Category("React1", "#0000FF"));
        Category category4 = dbHelper.insertCategory(new Category("React2", "#0000FF"));
        Category category5 = dbHelper.insertCategory(new Category("React3", "#0000FF"));
        Category category6 = dbHelper.insertCategory(new Category("React4", "#0000FF"));
        Category itTrendCategory = dbHelper.insertCategory(new Category("IT 트렌드", "#0000FF"));

        for (int i = 0; i < 5; i++) {
            dbHelper.insertHearit(TestFixture.createFixedHearitWith(category1));
            dbHelper.insertHearit(TestFixture.createFixedHearitWith(category2));
            dbHelper.insertHearit(TestFixture.createFixedHearitWith(category3));
            dbHelper.insertHearit(TestFixture.createFixedHearitWith(category4));
            dbHelper.insertHearit(TestFixture.createFixedHearitWith(category5));
            dbHelper.insertHearit(TestFixture.createFixedHearitWith(category6));
            dbHelper.insertHearit(TestFixture.createFixedHearitWith(itTrendCategory));
        }

        // when
        int categorySize = 5;
        int hearitSize = 5;
        List<RecommendationByCategoryResponse> responses = RestAssured.given(this.spec)
                .queryParam("categorySize", categorySize)
                .queryParam("hearitSize", hearitSize)
                .when()
                .get("/api/v1/recommendations/categories")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getList(".", RecommendationByCategoryResponse.class);

        // thena
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(responses).hasSize(categorySize);

            RecommendationByCategoryResponse exampleSingleResponse = responses.get(0);
            softly.assertThat(exampleSingleResponse.hearits()).hasSize(hearitSize);
            softly.assertThat(exampleSingleResponse.hearits().get(0).hearitId()).isNotNull();
            softly.assertThat(exampleSingleResponse.categoryId()).isNotNull();
        });
    }

    private String generateToken(Member member) {
        return jwtTokenProvider.createAccessToken(member.getId());
    }
}
