package com.onair.hearit.presentation;

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.document;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.onair.hearit.docs.ApiDocSnippets;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.dto.response.OriginalAudioResponse;
import com.onair.hearit.dto.response.ScriptResponse;
import com.onair.hearit.dto.response.ShortAudioResponse;
import com.onair.hearit.fixture.IntegrationTest;
import com.onair.hearit.fixture.TestFixture;
import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class FileSourceControllerTest extends IntegrationTest {

    @Test
    @DisplayName("원본 오디오 url 요청 시, 200 OK 및 id와 url을 반환한다.")
    void readOriginalAudioUrlWithSuccess() {
        // given
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        // when
        OriginalAudioResponse response = RestAssured.given(this.spec)
                .filter(document("filesource-read-original-audio",
                        resource(ResourceSnippetParameters.builder()
                                .tag("FileSource API")
                                .summary("원본 오디오 URL 조회")
                                .description("히어릿 ID를 통해 원본 오디오 파일의 URL을 조회합니다.")
                                .pathParameters(
                                        parameterWithName("hearitId").description("히어릿 ID")
                                )
                                .responseSchema(Schema.schema("OriginalAudioResponse"))
                                .responseFields(
                                        fieldWithPath("id").description("히어릿 ID"),
                                        fieldWithPath("url").description("원본 오디오 파일 URL")
                                )
                                .build())
                ))
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
                .filter(document("filesource-read-short-audio",
                        resource(ResourceSnippetParameters.builder()
                                .tag("FileSource API")
                                .summary("1분 미리듣기 오디오 URL 조회")
                                .description("히어릿 ID를 통해 1분 미리듣기 오디오 파일의 URL을 조회합니다.")
                                .pathParameters(
                                        parameterWithName("hearitId").description("히어릿 ID")
                                )
                                .responseSchema(Schema.schema("ShortAudioResponse"))
                                .responseFields(
                                        fieldWithPath("id").description("히어릿 ID"),
                                        fieldWithPath("url").description("1분 미리듣기 오디오 파일 URL")
                                )
                                .build())
                ))
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
                .filter(document("filesource-read-script",
                        resource(ResourceSnippetParameters.builder()
                                .tag("FileSource API")
                                .summary("스크립트 URL 조회")
                                .description("히어릿 ID를 통해 스크립트 파일의 URL을 조회합니다.")
                                .pathParameters(
                                        parameterWithName("hearitId").description("히어릿 ID")
                                )
                                .responseSchema(Schema.schema("ScriptResponse"))
                                .responseFields(
                                        fieldWithPath("id").description("히어릿 ID"),
                                        fieldWithPath("url").description("스크립트 파일 URL")
                                )
                                .build())
                ))
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
                .filter(document("filesource-read-not-found",
                        resource(ResourceSnippetParameters.builder()
                                .tag("FileSource API")
                                .summary("원본/미리듣기/스크립트 URL 조회")
                                .responseSchema(Schema.schema("ProblemDetail"))
                                .responseFields(ApiDocSnippets.getProblemDetailResponseFields())
                                .build())
                ))
                .when()
                .get("/api/v1/hearits/{hearitId}/script-url", notSavedHearitId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }
}
