package com.onair.hearit.app.hearit.presentation;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.onair.hearit.app.auth.infrastructure.jwt.TokenStatus;
import com.onair.hearit.app.common.dto.response.PagedResponse;
import com.onair.hearit.app.exception.custom.NotFoundException;
import com.onair.hearit.app.fixture.ControllerTest;
import com.onair.hearit.app.hearit.application.HearitService;
import com.onair.hearit.app.hearit.dto.HearitDetailResponse;
import com.onair.hearit.app.hearit.dto.HearitDetailResponse.CategoryResponse;
import com.onair.hearit.app.hearit.dto.HearitDetailResponse.LikeResponse;
import com.onair.hearit.app.hearit.dto.HearitDetailResponse.SourceResponse;
import com.onair.hearit.app.hearit.dto.HearitOverviewResponse;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(controllers = HearitController.class)
class HearitControllerTest extends ControllerTest {

    @MockitoBean
    private HearitService hearitService;

    @Test
    @DisplayName("단일 히어릿 조회 - 200 OK")
    void readHearitV1_OK() throws Exception {
        // given
        var hearitId = 3L;
        var response = new HearitDetailResponse(
                1L,
                "Title",
                "summary",
                List.of(new SourceResponse("source1", "url1"), new SourceResponse("source2", "url2")),
                300,
                (long) 1_000,
                LocalDateTime.of(2025, 9, 30, 10, 0),
                false,
                null,
                100,
                new CategoryResponse(2L, "categoryName", "#FFFFFF"),
                List.of(new HearitDetailResponse.KeywordResponse(1L, "keyword1"),
                        new HearitDetailResponse.KeywordResponse(2L, "keyword2")),
                new LikeResponse(10L, true)
        );

        given(jwtTokenProvider.getTokenStatus("valid-token")).willReturn(TokenStatus.VALID);
        given(jwtTokenProvider.getMemberUuid("valid-token")).willReturn(UUID.randomUUID());
        given(hearitService.getHearitDetail(any(), any())).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/hearits/{hearitId}", hearitId)
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andDo(document("v1-get-hearit-ok",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Hearit API")
                                .summary("단일 히어릿 조회 V1")
                                .description("""
                                        히어릿의 상세 정보를 조회합니다.

                                        로그인한 사용자의 경우, `isBookmarked`와 `bookmarkId` 필드가 사용자의 북마크 상태를 반영하여 반환됩니다.
                                        `like.isLiked`와 `like.count`에 좋아요 상태를 반영하여 반환됩니다.

                                        비로그인 사용자의 경우, `isBookmarked`는 항상 `false`이며 `bookmarkId`는 `null` 입니다.""")
                                .pathParameters(
                                        parameterWithName("hearitId").description("조회할 히어릿의 ID")
                                )
                                .responseFields(getHearitDetailResponseFields())
                                .build())
                ));
    }

    @Test
    @DisplayName("단일 히어릿 조회 - 404 Not Found")
    void readHearitV1_NotFound() throws Exception {
        // given
        Long notFoundHearitId = 9999L;

        given(jwtTokenProvider.getTokenStatus("valid-token")).willReturn(TokenStatus.VALID);
        given(jwtTokenProvider.getMemberUuid("valid-token")).willReturn(java.util.UUID.randomUUID());
        given(hearitService.getHearitDetail(any(), any())).willThrow(new NotFoundException("hearitId", "9999"));

        // when & then
        mockMvc.perform(get("/api/v1/hearits/{hearitId}", notFoundHearitId)
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isNotFound())
                .andDo(document("v1-get-hearit-not-found",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Hearit API")
                                .summary("단일 히어릿 조회 V1")
                                .responseFields(
                                        com.onair.hearit.fixture.ApiDocSnippets.getProblemDetailResponseFields())
                                .build())
                ));
    }

    @Test
    @DisplayName("히어릿 필터링 조회 V1 - 200 OK")
    void readFilteredHearitsV1_OK() throws Exception {
        // given
        var categoryId = 1L;
        var responses = List.of(new HearitOverviewResponse(101L, "Spring Boot Guide", 300, null,
                        LocalDateTime.of(2024, 9, 30, 10, 30),
                        List.of(new HearitOverviewResponse.KeywordResponse(1L, "spring"),
                                new HearitOverviewResponse.KeywordResponse(2L, "boot")),
                        new HearitOverviewResponse.CategoryResponse(3L, "Spring", "#FFFFFF")),
                new HearitOverviewResponse(102L, "JPA Tips", 200, null,
                        LocalDateTime.of(2024, 9, 29, 10, 0),
                        List.of(new HearitOverviewResponse.KeywordResponse(3L, "jpa"),
                                new HearitOverviewResponse.KeywordResponse(4L, "hibernate")),
                        new HearitOverviewResponse.CategoryResponse(2L, "JPA", "#EEFFFF")));
        var pagedResponses = PagedResponse.from(new PageImpl<>(responses, PageRequest.of(0, 20), responses.size()));

        given(jwtTokenProvider.getTokenStatus("valid-token")).willReturn(TokenStatus.VALID);
        given(jwtTokenProvider.getMemberUuid("valid-token")).willReturn(java.util.UUID.randomUUID());
        given(hearitService.getFilteredHearits(any(), any(), any(), any())).willReturn(pagedResponses);

        // when & then
        mockMvc.perform(get("/api/v1/hearits")
                        .header("Authorization", "Bearer valid-token")
                        .param("sort", "createdAt,desc")
                        .param("categoryId", String.valueOf(categoryId))
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andDo(document("v1-get-filtered-hearits-ok",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Hearit API")
                                .summary("히어릿 필터링 조회 V1")
                                .description("카테고리 및 정렬 조건에 대해 필터링된 히어릿 목록을 `Page` 단위로 조회합니다.")
                                .queryParameters(
                                        parameterWithName("categoryId").description("조회할 카테고리의 ID").optional(),
                                        parameterWithName("sort").description("정렬 조건(ex.{createdAt,desc})")
                                                .defaultValue("createdAt,desc"),
                                        parameterWithName("page").description("페이지 번호 (0부터 시작)").defaultValue("0"),
                                        parameterWithName("size").description("페이지 당 항목 수 (기본 20)").defaultValue("20")
                                )
                                .responseFields(Stream.concat(
                                                Arrays.stream(new FieldDescriptor[]{
                                                        fieldWithPath("content[].id").description("히어릿 ID"),
                                                        fieldWithPath("content[].title").description("히어릿 제목"),
                                                        fieldWithPath("content[].playTime").description("히어릿 재생 시간(초)"),
                                                        fieldWithPath("content[].lastPlayTime").description(
                                                                "히어릿 마지막 재생 시간(ms)").optional(),
                                                        fieldWithPath("content[].createdAt").description("히어릿 생성 일시"),
                                                        fieldWithPath("content[].keywords[].id").description("키워드 ID"),
                                                        fieldWithPath("content[].keywords[].name").description("키워드 이름"),
                                                        fieldWithPath("content[].category.id").description("카테고리 ID"),
                                                        fieldWithPath("content[].category.name").description("카테고리 이름"),
                                                        fieldWithPath("content[].category.colorCode").description("카테고리 컬러코드")
                                                }),
                                                Arrays.stream(
                                                        com.onair.hearit.fixture.ApiDocSnippets.getCustomPagedResponseFields())
                                        ).toArray(FieldDescriptor[]::new)
                                )
                                .build())
                ));
    }

    @Test
    @DisplayName("히어릿 필터링 조회 V1 - 400 Bad Request")
    void readFilteredHearitsV1_BadRequest() throws Exception {
        // given
        given(jwtTokenProvider.getTokenStatus("valid-token")).willReturn(TokenStatus.VALID);
        given(jwtTokenProvider.getMemberUuid("valid-token")).willReturn(java.util.UUID.randomUUID());

        // when & then
        mockMvc.perform(get("/api/v1/hearits")
                        .header("Authorization", "Bearer valid-token")
                        .param("sort", "unknown-field")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isBadRequest())
                .andDo(document("v1-get-filtered-hearits-bad-request",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Hearit API")
                                .summary("히어릿 필터링 조회 V1")
                                .description("카테고리 및 정렬 조건에 대해 필터링된 히어릿 목록을 `Page` 단위로 조회합니다.")
                                .responseFields(
                                        com.onair.hearit.fixture.ApiDocSnippets.getProblemDetailResponseFields()
                                )
                                .build())
                ));
    }

    @Test
    @DisplayName("히어릿 조회수 증가 - 204 NoContent")
    void increaseViewCount_OK() throws Exception {
        // given
        Long hearitId = 1L;

        given(jwtTokenProvider.getTokenStatus("valid-token")).willReturn(TokenStatus.VALID);
        given(jwtTokenProvider.getMemberUuid("valid-token")).willReturn(UUID.randomUUID());

        // when & then
        mockMvc.perform(post("/api/v1/hearits/{hearitId}/view", hearitId)
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isNoContent())
                .andDo(document("v1-increase-hearit-view-no-content",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Hearit API")
                                .summary("히어릿 조회수 증가 V1")
                                .description("""
                                        히어릿의 조회수를 1 증가시킵니다.

                                        헤더의 `userUUID`와 `hearitId`로 중복 키를 생성해 10초 내에는 한 번의 조회수만 증가됩니다."""
                                )
                                .pathParameters(
                                        parameterWithName("hearitId").description("조회수를 증가시킬 히어릿 ID")
                                )
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("히어릿 조회수 증가 - 404 Not Found")
    void increaseViewCount_NotFound() throws Exception {
        // given
        Long notFoundHearitId = 9999L;

        given(jwtTokenProvider.getTokenStatus("valid-token")).willReturn(TokenStatus.VALID);
        given(jwtTokenProvider.getMemberUuid("valid-token")).willReturn(UUID.randomUUID());

        willThrow(new NotFoundException("hearitId", notFoundHearitId.toString()))
                .given(hearitService)
                .increaseViewCount(eq(notFoundHearitId), any());

        // when & then
        mockMvc.perform(post("/api/v1/hearits/{hearitId}/view", notFoundHearitId)
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isNotFound())
                .andDo(document("v1-increase-hearit-view-not-found",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Hearit API")
                                .summary("히어릿 조회수 증가 V1")
                                .responseFields(
                                        com.onair.hearit.fixture.ApiDocSnippets
                                                .getProblemDetailResponseFields()
                                )
                                .build()
                        )
                ));
    }

    private FieldDescriptor[] getHearitDetailResponseFields() {
        return new FieldDescriptor[]{
                fieldWithPath("id").type(JsonFieldType.NUMBER).description("히어릿 ID"),
                fieldWithPath("title").type(JsonFieldType.STRING).description("히어릿 제목"),
                fieldWithPath("summary").type(JsonFieldType.STRING).description("히어릿 요약"),
                fieldWithPath("sources").description("히어릿의 출처 정보 목록"),
                fieldWithPath("sources[].sourceName").description("출처의 이름"),
                fieldWithPath("sources[].sourceUrl").description("출처의 URL"),
                fieldWithPath("playTime").type(JsonFieldType.NUMBER).description("재생 시간(초)"),
                fieldWithPath("lastPlayTime").type(JsonFieldType.NUMBER).description("마지막 재생 시간(ms)").optional(),
                fieldWithPath("createdAt").type(JsonFieldType.STRING).description("생성 일시"),
                fieldWithPath("isBookmarked").type(JsonFieldType.BOOLEAN).description("현재 사용자의 북마크 여부"),
                fieldWithPath("bookmarkId").type(JsonFieldType.NUMBER).description("북마크 ID (북마크된 경우)").optional(),
                fieldWithPath("viewCount").type(JsonFieldType.NUMBER).description("히어릿 조회수"),
                fieldWithPath("category").description("카테고리 정보"),
                fieldWithPath("category.id").type(JsonFieldType.NUMBER).description("카테고리 아이디"),
                fieldWithPath("category.name").type(JsonFieldType.STRING).description("카테고리 이름"),
                fieldWithPath("category.colorCode").type(JsonFieldType.STRING).description("카테고리 컬러코드"),
                fieldWithPath("keywords").type(JsonFieldType.ARRAY).description("키워드 목록"),
                fieldWithPath("keywords[].id").type(JsonFieldType.NUMBER).description("키워드 ID"),
                fieldWithPath("keywords[].name").type(JsonFieldType.STRING).description("키워드 이름"),
                fieldWithPath("like.count").type(JsonFieldType.NUMBER).description("좋아요 수"),
                fieldWithPath("like.isLiked").type(JsonFieldType.BOOLEAN).description("좋아요 유무")
        };
    }
}
