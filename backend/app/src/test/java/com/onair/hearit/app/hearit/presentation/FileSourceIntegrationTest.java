package com.onair.hearit.app.hearit.presentation;

import static com.toomuchcoding.jsonassert.JsonAssertion.assertThat;

import com.onair.hearit.app.fixture.IntegrationTest;
import com.onair.hearit.app.hearit.dto.OriginalAudioResponse;
import com.onair.hearit.app.hearit.dto.ScriptResponse;
import com.onair.hearit.app.hearit.dto.ShortAudioResponse;
import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.fixture.TestFixture;
import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class FileSourceIntegrationTest extends IntegrationTest {

    @Test
    @DisplayName("원본 오디오 url 요청 시, 200 OK 및 id와 url을 반환한다.")
    void readOriginalAudioUrlWithSuccess() {
        // given
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        // when
        OriginalAudioResponse response = RestAssured.given(this.spec)
                .when()
                .get("/api/v1/hearits/{hearitId}/original-audio-url", hearit.getId())
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
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        // when
        ShortAudioResponse response = RestAssured.given(this.spec)
                .when()
                .get("/api/v1/hearits/{hearitId}/short-audio-url", hearit.getId())
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
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        // when
        ScriptResponse response = RestAssured.given(this.spec)
                .when()
                .get("/api/v1/hearits/{hearitId}/script-url", hearit.getId())
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
        Long notSavedHearitId = 9999L;

        // when & then
        RestAssured.given(this.spec)
                .when()
                .get("/api/v1/hearits/{hearitId}/script-url", notSavedHearitId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }
}
