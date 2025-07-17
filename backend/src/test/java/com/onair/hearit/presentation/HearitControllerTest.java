package com.onair.hearit.presentation;

import static org.assertj.core.api.Assertions.assertThat;

import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.dto.response.HearitDetailResponse;
import io.restassured.RestAssured;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class HearitControllerTest extends IntegrationTest {

    @Test
    @DisplayName("히어릿 단일 조회 시, 200 OK 및 히어릿 정보를 제공한다.")
    void readHearitWithSuccess() {
        // given
        Hearit hearit = saveHearit(1);

        // when & then
        HearitDetailResponse response = RestAssured.when()
                .get("/api/v1/hearits/" + hearit.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract().as(HearitDetailResponse.class);

        assertThat(response.id()).isEqualTo(hearit.getId());
    }

    @Test
    @DisplayName("히어릿 단일 조회 시, 존재하지 않는 아이디인 경우 404 NOT_FOUND를 반환한다.")
    void readHearitWithNotFound() {
        // given
        Long notFoundHearitId = 1L;

        // when & then
        RestAssured.when()
                .get("/api/v1/hearits/" + notFoundHearitId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    private Hearit saveHearit(int num) {
        Category category = new Category("name" + num);
        dbHelper.insertCategory(category);

        Hearit hearit = new Hearit(
                "title" + num,
                "summary" + num, num,
                "originalAudioUrl" + num,
                "shortAudioUrl" + num,
                "scriptUrl" + num,
                "source" + num,
                LocalDateTime.now(),
                category);
        return dbHelper.insertHearit(hearit);
    }
}
