package com.onair.hearit.app.recommendation.presentation;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.headerWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.onair.hearit.app.auth.infrastructure.jwt.TokenStatus;
import com.onair.hearit.app.fixture.ControllerTest;
import com.onair.hearit.app.recommendation.application.RecommendationService;
import com.onair.hearit.app.recommendation.dto.RecommendationByCategoryResponse;
import com.onair.hearit.app.recommendation.dto.RecommendationByCategoryResponse.HearitResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(controllers = RecommendationController.class)
class RecommendationControllerTest extends ControllerTest {

    @MockitoBean
    RecommendationService recommendationService;

    @Test
    @DisplayName("추천 카테고리별 히어릿 조회 V1 - 200 OK") // To be Deprecated
    void readHearitsWithRecommendCategoryV1_OK() throws Exception {
        // given
        var itTrendCategory = new RecommendationByCategoryResponse(1L, "IT 트랜드", "#FF0000",
                List.of(
                        new HearitResponse(101L, "Hearit 101", LocalDateTime.now()),
                        new HearitResponse(102L, "Hearit 102", LocalDateTime.now()),
                        new HearitResponse(103L, "Hearit 103", LocalDateTime.now()),
                        new HearitResponse(104L, "Hearit 104", LocalDateTime.now()),
                        new HearitResponse(105L, "Hearit 105", LocalDateTime.now())
                )
        );
        var category1 = new RecommendationByCategoryResponse(2L, "Category A", "#FF0000",
                List.of(
                        new HearitResponse(201L, "Hearit 201", LocalDateTime.now()),
                        new HearitResponse(202L, "Hearit 202", LocalDateTime.now()),
                        new HearitResponse(203L, "Hearit 203", LocalDateTime.now()),
                        new HearitResponse(204L, "Hearit 204", LocalDateTime.now()),
                        new HearitResponse(205L, "Hearit 205", LocalDateTime.now())
                )
        );
        var category2 = new RecommendationByCategoryResponse(3L, "Category B", "#00FF00",
                List.of(
                        new HearitResponse(301L, "Hearit 301", LocalDateTime.now()),
                        new HearitResponse(302L, "Hearit 302", LocalDateTime.now()),
                        new HearitResponse(303L, "Hearit 303", LocalDateTime.now()),
                        new HearitResponse(304L, "Hearit 304", LocalDateTime.now()),
                        new HearitResponse(305L, "Hearit 305", LocalDateTime.now())
                )
        );
        var category3 = new RecommendationByCategoryResponse(4L, "Category C", "#0000FF",
                List.of(
                        new HearitResponse(401L, "Hearit 401", LocalDateTime.now()),
                        new HearitResponse(402L, "Hearit 402", LocalDateTime.now()),
                        new HearitResponse(403L, "Hearit 403", LocalDateTime.now()),
                        new HearitResponse(404L, "Hearit 404", LocalDateTime.now()),
                        new HearitResponse(405L, "Hearit 405", LocalDateTime.now())
                )
        );
        var randomCategory = new RecommendationByCategoryResponse(5L, "Random Category", "#0000FF",
                List.of(
                        new HearitResponse(501L, "Hearit 501", LocalDateTime.now()),
                        new HearitResponse(502L, "Hearit 502", LocalDateTime.now()),
                        new HearitResponse(503L, "Hearit 503", LocalDateTime.now()),
                        new HearitResponse(504L, "Hearit 504", LocalDateTime.now()),
                        new HearitResponse(505L, "Hearit 505", LocalDateTime.now())
                )
        );
        var mockedResponse = List.of(itTrendCategory, category1, category2, category3, randomCategory);

        given(recommendationService.getCategoryRecommendations(any(), anyInt(), anyInt())).willReturn(mockedResponse);

        // when & then
        mockMvc.perform(get("/api/v1/hearits/recommend-category")
                        .param("categorySize", "5")
                        .param("hearitSize", "5"))
                .andExpect(status().isOk())
                .andDo(document("v1-get-hearits-recommend-category-ok",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Hearit API")
                                .summary("추천 카테고리별 히어릿 조회 V1 (Deprecated - 호환성 유지용)")
                                .description("추천하는 5개 카테고리와 카테고리별로 그룹화된 히어릿 5개 목록을 조회합니다.")
                                .responseFields(
                                        fieldWithPath("[].categoryId").description("카테고리 ID"),
                                        fieldWithPath("[].categoryName").description("카테고리 이름"),
                                        fieldWithPath("[].colorCode").description("카테고리 색상 코드"),
                                        fieldWithPath("[].hearits").description("해당 카테고리의 최신 히어릿 목록"),
                                        fieldWithPath("[].hearits[].hearitId").description("히어릿 ID"),
                                        fieldWithPath("[].hearits[].title").description("히어릿 제목"),
                                        fieldWithPath("[].hearits[].createdAt").description("히어릿 생성 일시")
                                )
                                .build())
                ));
    }

    @Test
    @DisplayName("회원 추천 카테고리별 히어릿 조회 V1 - 200 OK")
    void readRecommendationsByCategory_withMember_V1_OK() throws Exception {
        // given
        var itTrendCategory = new RecommendationByCategoryResponse(1L, "IT 트랜드", "#FF0000",
                List.of(
                        new HearitResponse(101L, "Hearit 101", LocalDateTime.now()),
                        new HearitResponse(102L, "Hearit 102", LocalDateTime.now()),
                        new HearitResponse(103L, "Hearit 103", LocalDateTime.now()),
                        new HearitResponse(104L, "Hearit 104", LocalDateTime.now()),
                        new HearitResponse(105L, "Hearit 105", LocalDateTime.now())
                )
        );
        var category1 = new RecommendationByCategoryResponse(2L, "Category A", "#FF0000",
                List.of(
                        new HearitResponse(201L, "Hearit 201", LocalDateTime.now()),
                        new HearitResponse(202L, "Hearit 202", LocalDateTime.now()),
                        new HearitResponse(203L, "Hearit 203", LocalDateTime.now()),
                        new HearitResponse(204L, "Hearit 204", LocalDateTime.now()),
                        new HearitResponse(205L, "Hearit 205", LocalDateTime.now())
                )
        );
        var category2 = new RecommendationByCategoryResponse(3L, "Category B", "#00FF00",
                List.of(
                        new HearitResponse(301L, "Hearit 301", LocalDateTime.now()),
                        new HearitResponse(302L, "Hearit 302", LocalDateTime.now()),
                        new HearitResponse(303L, "Hearit 303", LocalDateTime.now()),
                        new HearitResponse(304L, "Hearit 304", LocalDateTime.now()),
                        new HearitResponse(305L, "Hearit 305", LocalDateTime.now())
                )
        );
        var category3 = new RecommendationByCategoryResponse(4L, "Category C", "#0000FF",
                List.of(
                        new HearitResponse(401L, "Hearit 401", LocalDateTime.now()),
                        new HearitResponse(402L, "Hearit 402", LocalDateTime.now()),
                        new HearitResponse(403L, "Hearit 403", LocalDateTime.now()),
                        new HearitResponse(404L, "Hearit 404", LocalDateTime.now()),
                        new HearitResponse(405L, "Hearit 405", LocalDateTime.now())
                )
        );
        var randomCategory = new RecommendationByCategoryResponse(5L, "Random Category", "#0000FF",
                List.of(
                        new HearitResponse(501L, "Hearit 501", LocalDateTime.now()),
                        new HearitResponse(502L, "Hearit 502", LocalDateTime.now()),
                        new HearitResponse(503L, "Hearit 503", LocalDateTime.now()),
                        new HearitResponse(504L, "Hearit 504", LocalDateTime.now()),
                        new HearitResponse(505L, "Hearit 505", LocalDateTime.now())
                )
        );
        var mockedResponse = List.of(itTrendCategory, category1, category2, category3, randomCategory);

        given(jwtTokenProvider.getTokenStatus("valid-token")).willReturn(TokenStatus.VALID);
        given(recommendationService.getCategoryRecommendations(any(), anyInt(), anyInt())).willReturn(mockedResponse);

        // when & then
        mockMvc.perform(get("/api/v1/recommendations/categories")
                        .header("Authorization", "Bearer valid-token")
                        .param("categorySize", "5")
                        .param("hearitSize", "5"))
                .andExpect(status().isOk())
                .andDo(document("v1-get-recommendations-by-category-member-ok",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Recommendation API")
                                .summary("추천 카테고리별 히어릿 조회 V1")
                                .description("요청에서 지정한 카테고리 개수와 히어릿 개수에 따라, 추천된 카테고리별 최신 히어릿 목록을 조회합니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("Bearer 토큰 (형식: `Bearer {JWT}`)").optional()
                                )
                                .queryParameters(
                                        parameterWithName("categorySize").description("추천받을 카테고리 개수 (기본 5, 최대 20)").defaultValue("5"),
                                        parameterWithName("hearitSize").description("카테고리 당 히어릿 개수 (기본 5, 최대 30)").defaultValue("5")
                                )
                                .responseFields(
                                        fieldWithPath("[].categoryId").description("카테고리 ID"),
                                        fieldWithPath("[].categoryName").description("카테고리 이름"),
                                        fieldWithPath("[].colorCode").description("카테고리 색상 코드"),
                                        fieldWithPath("[].hearits").description("해당 카테고리의 최신 히어릿 목록"),
                                        fieldWithPath("[].hearits[].hearitId").description("히어릿 ID"),
                                        fieldWithPath("[].hearits[].title").description("히어릿 제목"),
                                        fieldWithPath("[].hearits[].createdAt").description("히어릿 생성 일시")
                                )
                                .build())
                ));
    }

    @Test
    @DisplayName("비회원 추천 카테고리별 히어릿 조회 V1 - 200 OK")
    void readRecommendationsByCategory_withGuest_V1_OK() throws Exception {
        // given
        var itTrendCategory = new RecommendationByCategoryResponse(1L, "IT 트랜드", "#FF0000",
                List.of(
                        new HearitResponse(101L, "Hearit 101", LocalDateTime.now()),
                        new HearitResponse(102L, "Hearit 102", LocalDateTime.now()),
                        new HearitResponse(103L, "Hearit 103", LocalDateTime.now()),
                        new HearitResponse(104L, "Hearit 104", LocalDateTime.now()),
                        new HearitResponse(105L, "Hearit 105", LocalDateTime.now())
                )
        );
        var category1 = new RecommendationByCategoryResponse(2L, "Category A", "#FF0000",
                List.of(
                        new HearitResponse(201L, "Hearit 201", LocalDateTime.now()),
                        new HearitResponse(202L, "Hearit 202", LocalDateTime.now()),
                        new HearitResponse(203L, "Hearit 203", LocalDateTime.now()),
                        new HearitResponse(204L, "Hearit 204", LocalDateTime.now()),
                        new HearitResponse(205L, "Hearit 205", LocalDateTime.now())
                )
        );
        var category2 = new RecommendationByCategoryResponse(3L, "Category B", "#00FF00",
                List.of(
                        new HearitResponse(301L, "Hearit 301", LocalDateTime.now()),
                        new HearitResponse(302L, "Hearit 302", LocalDateTime.now()),
                        new HearitResponse(303L, "Hearit 303", LocalDateTime.now()),
                        new HearitResponse(304L, "Hearit 304", LocalDateTime.now()),
                        new HearitResponse(305L, "Hearit 305", LocalDateTime.now())
                )
        );
        var category3 = new RecommendationByCategoryResponse(4L, "Category C", "#0000FF",
                List.of(
                        new HearitResponse(401L, "Hearit 401", LocalDateTime.now()),
                        new HearitResponse(402L, "Hearit 402", LocalDateTime.now()),
                        new HearitResponse(403L, "Hearit 403", LocalDateTime.now()),
                        new HearitResponse(404L, "Hearit 404", LocalDateTime.now()),
                        new HearitResponse(405L, "Hearit 405", LocalDateTime.now())
                )
        );
        var randomCategory = new RecommendationByCategoryResponse(5L, "Random Category", "#0000FF",
                List.of(
                        new HearitResponse(501L, "Hearit 501", LocalDateTime.now()),
                        new HearitResponse(502L, "Hearit 502", LocalDateTime.now()),
                        new HearitResponse(503L, "Hearit 503", LocalDateTime.now()),
                        new HearitResponse(504L, "Hearit 504", LocalDateTime.now()),
                        new HearitResponse(505L, "Hearit 505", LocalDateTime.now())
                )
        );
        var mockedResponse = List.of(itTrendCategory, category1, category2, category3, randomCategory);

        given(recommendationService.getCategoryRecommendations(any(), anyInt(), anyInt())).willReturn(mockedResponse);

        // when & then
        mockMvc.perform(get("/api/v1/recommendations/categories")
                        .param("categorySize", "5")
                        .param("hearitSize", "5"))
                .andExpect(status().isOk())
                .andDo(document("v1-get-recommendations-by-category-guest-ok",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Recommendation API")
                                .summary("추천 카테고리별 히어릿 조회 V1")
                                .description("요청에서 지정한 카테고리 개수와 히어릿 개수에 따라, 추천된 카테고리별 최신 히어릿 목록을 조회합니다.")
                                .queryParameters(
                                        parameterWithName("categorySize").description("추천받을 카테고리 개수 (기본 5, 최대 20)").defaultValue("5"),
                                        parameterWithName("hearitSize").description("카테고리 당 히어릿 개수 (기본 5, 최대 30)").defaultValue("5")
                                )
                                .responseFields(
                                        fieldWithPath("[].categoryId").description("카테고리 ID"),
                                        fieldWithPath("[].categoryName").description("카테고리 이름"),
                                        fieldWithPath("[].colorCode").description("카테고리 색상 코드"),
                                        fieldWithPath("[].hearits").description("해당 카테고리의 최신 히어릿 목록"),
                                        fieldWithPath("[].hearits[].hearitId").description("히어릿 ID"),
                                        fieldWithPath("[].hearits[].title").description("히어릿 제목"),
                                        fieldWithPath("[].hearits[].createdAt").description("히어릿 생성 일시")
                                )
                                .build())
                ));
    }
}
