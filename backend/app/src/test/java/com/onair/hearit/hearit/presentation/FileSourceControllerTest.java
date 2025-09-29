package com.onair.hearit.hearit.presentation;

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.onair.hearit.exception.custom.NotFoundException;
import com.onair.hearit.fixture.ApiDocSnippets;
import com.onair.hearit.fixture.ControllerTest;
import com.onair.hearit.hearit.application.FileSourceService;
import com.onair.hearit.hearit.dto.OriginalAudioResponse;
import com.onair.hearit.hearit.dto.ScriptResponse;
import com.onair.hearit.hearit.dto.ShortAudioResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(controllers = FileSourceController.class)
class FileSourceControllerTest extends ControllerTest {

    @MockitoBean
    private FileSourceService fileSourceService;

    @Test
    @DisplayName("원본 오디오 조회 - 200 OK")
    void readOriginalAudioUrlV1_OK() throws Exception {
        // given
        var hearitId = 3L;
        var response = new OriginalAudioResponse(1L, "http://hearit.com/original-audio-url");

        given(fileSourceService.getOriginalAudio(any())).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/hearits/{hearitId}/original-audio-url", hearitId))
                .andExpect(status().isOk())
                .andDo(MockMvcRestDocumentationWrapper.document("v1-get-hearits-original-audio-url-ok",
                        resource(ResourceSnippetParameters.builder()
                                .tag("FileSource API")
                                .summary("원본 오디오 조회 V1")
                                .description("히어릿에 대한 원본 오디오 파일 URL을 조회합니다.")
                                .pathParameters(
                                        parameterWithName("hearitId").description("대상 히어릿 ID")
                                )
                                .responseFields(
                                        fieldWithPath("id").description("히어릿 ID"),
                                        fieldWithPath("url").description("원본 오디오 파일 URL")
                                )
                                .build())
                ));
    }

    @Test
    @DisplayName("1분 오디오 조회 - 200 OK")
    void readShortAudioUrlV1_OK() throws Exception {
        // given
        var hearitId = 3L;
        var response = new ShortAudioResponse(1L, "http://hearit.com/short-audio-url");

        given(fileSourceService.getShortAudio(any())).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/hearits/{hearitId}/short-audio-url", hearitId))
                .andExpect(status().isOk())
                .andDo(MockMvcRestDocumentationWrapper.document("v1-get-hearits-short-audio-url-ok",
                        resource(ResourceSnippetParameters.builder()
                                .tag("FileSource API")
                                .summary("1분 오디오 조회 V1")
                                .description("히어릿에 대한 1분 오디오 파일 URL을 조회합니다.")
                                .pathParameters(
                                        parameterWithName("hearitId").description("대상 히어릿 ID")
                                )
                                .responseFields(
                                        fieldWithPath("id").description("히어릿 ID"),
                                        fieldWithPath("url").description("1분 오디오 파일 URL")
                                )
                                .build())
                ));
    }

    @Test
    @DisplayName("대본 조회 - 200 OK")
    void readScriptUrlV1_OK() throws Exception {
        // given
        var hearitId = 3L;
        var response = new ScriptResponse(1L, "http://hearit.com/script-url");

        given(fileSourceService.getScript(any())).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/hearits/{hearitId}/script-url", hearitId))
                .andExpect(status().isOk())
                .andDo(MockMvcRestDocumentationWrapper.document("v1-get-hearits-script-url-ok",
                        resource(ResourceSnippetParameters.builder()
                                .tag("FileSource API")
                                .summary("대본 조회 V1")
                                .description("히어릿에 대한 대본 파일 URL을 조회합니다.")
                                .pathParameters(
                                        parameterWithName("hearitId").description("대상 히어릿 ID")
                                )
                                .responseFields(
                                        fieldWithPath("id").description("히어릿 ID"),
                                        fieldWithPath("url").description("대본 파일 URL")
                                )
                                .build())
                ));
    }

    @Test
    @DisplayName("대본 조회 - 404 Not Found")
    void readScriptUrlV1_NotFound() throws Exception {
        // given
        var notSavedHearitId = 9999L;

        given(fileSourceService.getScript(any()))
                .willThrow(new NotFoundException("hearitId", "9999"));

        // when & then
        mockMvc.perform(get("/api/v1/hearits/{hearitId}/script-url", notSavedHearitId))
                .andExpect(status().isNotFound())
                .andDo(MockMvcRestDocumentationWrapper.document("v1-get-hearits-script-url-not-found",
                        resource(ResourceSnippetParameters.builder()
                                .tag("FileSource API")
                                .summary("대본 조회 V1")
                                .responseFields(ApiDocSnippets.getProblemDetailResponseFields())
                                .build())
                ));
    }
}
