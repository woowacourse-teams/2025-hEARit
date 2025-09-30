package com.onair.hearit.recommendhearit.presentation;

import static org.assertj.core.api.Assertions.assertThat;

import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.RecommendHearit;
import com.onair.hearit.fixture.IntegrationTest;
import com.onair.hearit.recommendhearit.dto.RecommendHearitResponse;
import io.restassured.RestAssured;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class RecommendHearitIntegrationTest extends IntegrationTest {

    @Test
    @DisplayName("오늘의 추천 히어릿을 조회 시, 200 OK 및 5개 히어릿 정보 목록을 제공한다.")
    void readRecommendedHearits() {
        // given
        LocalDate today = LocalDate.now();
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        for (int i = 0; i < 5; i++) {
            Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
            dbHelper.insertRecommendHearit(new RecommendHearit(hearit, today));
        }

        // when
        List<RecommendHearitResponse> responses = RestAssured.given(this.spec)
                .when()
                .get("/api/v1/hearits/recommend")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getList(".", RecommendHearitResponse.class);

        // then
        assertThat(responses).hasSize(5);
    }
}
