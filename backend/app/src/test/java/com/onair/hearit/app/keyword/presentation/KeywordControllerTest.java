package com.onair.hearit.app.keyword.presentation;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.onair.hearit.app.exception.custom.NotFoundException;
import com.onair.hearit.app.fixture.ControllerTest;
import com.onair.hearit.app.keyword.application.KeywordService;
import com.onair.hearit.app.keyword.dto.KeywordResponse;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(controllers = KeywordController.class)
class KeywordControllerTest extends ControllerTest {

    @MockitoBean
    private KeywordService keywordService;

    @Test
    @DisplayName("키워드 조회 V1 - 200 OK")
    void readKeywordsV1_OK() throws Exception {
        // given
        var responses = List.of(
                new KeywordResponse(1L, "Spring"),
                new KeywordResponse(2L, "Java")
        );

        given(keywordService.getKeywords(any())).willReturn(responses);

        // when & then
        mockMvc.perform(get("/api/v1/keywords")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andDo(document("v1-get-keywords-ok",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Keyword API")
                                .summary("전체 키워드 목록 조회 V1")
                                .description("전체 키워드 목록을 `Page` 단위로 조회합니다.")
                                .queryParameters(
                                        parameterWithName("page").description("페이지 번호 (0부터 시작)").defaultValue("0"),
                                        parameterWithName("size").description("페이지 당 항목 수 (기본 20)").defaultValue("20")
                                )
                                .responseFields(
                                        fieldWithPath("[].id").description("키워드 ID"),
                                        fieldWithPath("[].name").description("키워드 이름")
                                )
                                .build())
                ));
    }

    @Test
    @DisplayName("단일 키워드 조회 V1 - 200 OK")
    void readKeywordV1_OK() throws Exception {
        // given
        var response = new KeywordResponse(1L, "Spring");

        given(keywordService.getKeyword(any())).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/keywords/{keywordId}", response.id()))
                .andExpect(status().isOk())
                .andDo(document("v1-get-keyword-ok",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Keyword API")
                                .summary("단일 키워드 조회 V1")
                                .description("단일 키워드의 정보를 조회합니다.")
                                .pathParameters(
                                        parameterWithName("keywordId").description("조회할 키워드의 ID")
                                )
                                .responseFields(
                                        fieldWithPath("id").description("키워드 ID"),
                                        fieldWithPath("name").description("키워드 이름")
                                )
                                .build())
                ));
    }

    @Test
    @DisplayName("단일 키워드 조회 V1 - 404 Not Found")
    void readNotFoundKeyword() throws Exception {
        // given
        var notFoundKeywordId = 9999L;

        given(keywordService.getKeyword(any())).willThrow(new NotFoundException("keywordId", "9999"));

        // when & then
        mockMvc.perform(get("/api/v1/keywords/{keywordId}", notFoundKeywordId))
                .andExpect(status().isNotFound())
                .andDo(document("v1-get-keywords-not-found",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Keyword API")
                                .summary("단일 키워드 조회 V1")
                                .responseFields(com.onair.hearit.fixture.ApiDocSnippets.getProblemDetailResponseFields())
                                .build())
                ));
    }
}
