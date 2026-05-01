package com.onair.hearit.app.series.presentation;

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.onair.hearit.app.exception.custom.NotFoundException;
import com.onair.hearit.app.fixture.ControllerTest;
import com.onair.hearit.app.series.application.SeriesService;
import com.onair.hearit.app.series.dto.SeriesDetailResponse;
import com.onair.hearit.app.series.dto.SeriesDetailResponse.SeriesHearitResponse;
import com.onair.hearit.app.series.dto.SeriesOverviewResponse;
import com.onair.hearit.app.common.dto.response.PagedResponse;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(controllers = SeriesController.class)
class SeriesControllerTest extends ControllerTest {

    @MockitoBean
    private SeriesService seriesService;

    @Test
    @DisplayName("시리즈 목록 조회 V1 - 200 OK")
    void readSeriesList_OK() throws Exception {
        // given
        var responses = IntStream.range(1, 4)
                .mapToObj(i -> new SeriesOverviewResponse(
                        (long) i,
                        "시리즈" + i,
                        "시리즈 설명" + i,
                        "/series/image/test" + i + ".jpg",
                        LocalDateTime.of(2026, 4, 9, 10, 0, 0)
                ))
                .toList();
        var pagedResponses = PagedResponse.from(
                new PageImpl<>(responses, PageRequest.of(0, 20), responses.size()));

        given(seriesService.getSeries(any())).willReturn(pagedResponses);

        // when & then
        mockMvc.perform(get("/api/v1/series")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andDo(MockMvcRestDocumentationWrapper.document("v1-get-series-ok",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Series API")
                                .summary("시리즈 목록 조회 V1")
                                .description("전체 시리즈 목록을 최신 등록 순으로 페이지 단위 조회합니다.")
                                .queryParameters(
                                        parameterWithName("page").description("페이지 번호 (start 0)").defaultValue("0"),
                                        parameterWithName("size").description("페이지 당 항목 수").defaultValue("20")
                                )
                                .responseFields(Stream.concat(
                                        Arrays.stream(new FieldDescriptor[]{
                                                fieldWithPath("content[].id").description("시리즈 ID"),
                                                fieldWithPath("content[].title").description("시리즈 제목"),
                                                fieldWithPath("content[].description").description("시리즈 설명").optional(),
                                                fieldWithPath("content[].imageUrl").description("시리즈 이미지 S3 키").optional(),
                                                fieldWithPath("content[].createdAt").description("시리즈 생성 시각")
                                        }),
                                        Arrays.stream(com.onair.hearit.fixture.ApiDocSnippets.getCustomPagedResponseFields())
                                ).toArray(FieldDescriptor[]::new))
                                .build())
                ));
    }

    @Test
    @DisplayName("시리즈 목록 조회 V1 - 400 Bad Request")
    void readSeriesList_BadRequest() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/series")
                        .param("page", "-1")
                        .param("size", "20"))
                .andExpect(status().isBadRequest())
                .andDo(MockMvcRestDocumentationWrapper.document("v1-get-series-bad-request",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Series API")
                                .summary("시리즈 목록 조회 V1")
                                .responseFields(com.onair.hearit.fixture.ApiDocSnippets.getProblemDetailResponseFields())
                                .build())
                ));
    }

    @Test
    @DisplayName("시리즈 상세 조회 V1 - 200 OK")
    void readSeriesDetail_OK() throws Exception {
        // given
        var hearits = List.of(
                new SeriesHearitResponse(1L, "1화: 에피소드 제목", 360,
                        LocalDateTime.of(2026, 4, 1, 9, 0, 0)),
                new SeriesHearitResponse(2L, "2화: 에피소드 제목", 420,
                        LocalDateTime.of(2026, 4, 5, 9, 0, 0))
        );
        var detailResponse = new SeriesDetailResponse(
                1L, "테코톡 모음", "시리즈 설명",
                "/series/image/test.jpg",
                LocalDateTime.of(2026, 4, 9, 10, 0, 0),
                hearits
        );

        given(seriesService.getSeriesDetail(eq(1L))).willReturn(detailResponse);

        // when & then
        mockMvc.perform(get("/api/v1/series/{seriesId}", 1L))
                .andExpect(status().isOk())
                .andDo(MockMvcRestDocumentationWrapper.document("v1-get-series-detail-ok",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Series API")
                                .summary("시리즈 상세 조회 V1")
                                .description("시리즈 정보와 해당 시리즈에 속한 에피소드 목록을 조회합니다.")
                                .responseFields(
                                        fieldWithPath("id").description("시리즈 ID"),
                                        fieldWithPath("title").description("시리즈 제목"),
                                        fieldWithPath("description").description("시리즈 설명").optional(),
                                        fieldWithPath("imageUrl").description("시리즈 이미지 S3 키").optional(),
                                        fieldWithPath("createdAt").description("시리즈 생성 시각"),
                                        fieldWithPath("hearits[].id").description("에피소드 ID"),
                                        fieldWithPath("hearits[].title").description("에피소드 제목"),
                                        fieldWithPath("hearits[].playTime").description("재생 시간 (초)"),
                                        fieldWithPath("hearits[].createdAt").description("에피소드 생성 시각")
                                )
                                .build())
                ));
    }

    @Test
    @DisplayName("시리즈 상세 조회 V1 - 404 Not Found")
    void readSeriesDetail_NotFound() throws Exception {
        // given
        given(seriesService.getSeriesDetail(eq(99999L)))
                .willThrow(new NotFoundException("seriesId", "99999"));

        // when & then
        mockMvc.perform(get("/api/v1/series/{seriesId}", 99999L))
                .andExpect(status().isNotFound())
                .andDo(MockMvcRestDocumentationWrapper.document("v1-get-series-detail-not-found",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Series API")
                                .summary("시리즈 상세 조회 V1")
                                .responseFields(com.onair.hearit.fixture.ApiDocSnippets.getProblemDetailResponseFields())
                                .build())
                ));
    }
}
