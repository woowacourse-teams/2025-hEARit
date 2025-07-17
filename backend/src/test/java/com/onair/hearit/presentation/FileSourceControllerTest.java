package com.onair.hearit.presentation;

import static org.assertj.core.api.Assertions.assertThat;

import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.dto.response.OriginalAudioResponse;
import com.onair.hearit.dto.response.ScriptResponse;
import com.onair.hearit.dto.response.ShortAudioResponse;
import io.restassured.RestAssured;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class FileSourceControllerTest extends IntegrationTest {

    @Test
    @DisplayName("원본 오디오 url 요청 시, 200 OK 및 id와 url을 반환한다.")
    void readOriginalAudioUrlWithSuccess() {
        // given
        Hearit hearit = saveHearit(1);

        // when
        OriginalAudioResponse response = RestAssured.when()
                .get("/api/v1/hearits/" + hearit.getId() + "/original-audio-url")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract().as(OriginalAudioResponse.class);

        // then
        assertThat(response.url()).contains(hearit.getOriginalAudioUrl());
    }

    @Test
    @DisplayName("1분 오디오 url 요청 시, 200 OK 및 id와 url을 반환한다.")
    void readShortAudioUrlWithSuccess() {
        // given
        Hearit hearit = saveHearit(1);

        // when
        ShortAudioResponse response = RestAssured.when()
                .get("/api/v1/hearits/" + hearit.getId() + "/short-audio-url")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract().as(ShortAudioResponse.class);

        // then
        assertThat(response.url()).contains(hearit.getShortAudioUrl());
    }

    @Test
    @DisplayName("대본 url 요청 시 200 OK 및 id와 url을 반환한다.")
    void readScriptUrlWithSuccess() {
        // given
        Hearit hearit = saveHearit(1);

        // when
        ScriptResponse response = RestAssured.when()
                .get("/api/v1/hearits/" + hearit.getId() + "/script-url")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract().as(ScriptResponse.class);

        // then
        assertThat(response.url()).contains(hearit.getScriptUrl());
    }

    @Test
    @DisplayName("존재하지 않은 hearit id로 url 요청 시, 404 NOT_FOUND를 반환한다.")
    void notFoundHearitId() {
        // given
        Long notSavedHearitId = 1L;

        // when & then
        RestAssured.when()
                .get("/api/v1/hearits/" + notSavedHearitId + "/script-url")
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
