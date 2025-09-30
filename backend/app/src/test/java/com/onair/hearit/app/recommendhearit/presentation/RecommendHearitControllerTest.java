package com.onair.hearit.app.recommendhearit.presentation;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.onair.hearit.app.fixture.ControllerTest;
import com.onair.hearit.app.recommendhearit.application.RecommendHearitService;
import com.onair.hearit.app.recommendhearit.dto.RecommendHearitResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(controllers = RecommendHearitController.class)
class RecommendHearitControllerTest extends ControllerTest {

    @MockitoBean
    private RecommendHearitService recommendHearitService;

    @Test
    @DisplayName("오늘의 추천 히어릿 목록 조회 V1 - 200 OK")
    void readRecommendedHearitsV1_OK() throws Exception {
        // given
        var now = LocalDateTime.of(2025, 9, 30, 10, 0);
        var responses = List.of(
                new RecommendHearitResponse(1L, "히어릿1", 120, now, "카테고리1", "#000001"),
                new RecommendHearitResponse(2L, "히어릿2", 150, now, "카테고리2", "#000002"),
                new RecommendHearitResponse(3L, "히어릿3", 200, now, "카테고리3", "#000003"),
                new RecommendHearitResponse(4L, "히어릿4", 180, now, "카테고리4", "#000004"),
                new RecommendHearitResponse(5L, "히어릿5", 210, now, "카테고리5", "#000005")
        );

        given(recommendHearitService.getRecommendedHearits()).willReturn(responses);

        // when & then
        mockMvc.perform(get("/api/v1/hearits/recommend"))
                .andExpect(status().isOk())
                .andDo(document("v1-get-hearits-recommend-ok",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Hearit API")
                                .summary("오늘의 추천 히어릿 목록 조회 V1")
                                .description("추천 히어릿 목록을 최대 5개까지 조회합니다.")
                                .responseFields(
                                        fieldWithPath("[].id").description("히어릿 ID"),
                                        fieldWithPath("[].title").description("히어릿 제목"),
                                        fieldWithPath("[].playTime").description("재생 시간(초)"),
                                        fieldWithPath("[].createdAt").description("생성 일시"),
                                        fieldWithPath("[].categoryName").description("카테고리 이름"),
                                        fieldWithPath("[].categoryColor").description("카테고리 색상 코드")
                                )
                                .build())
                ));
    }
}
