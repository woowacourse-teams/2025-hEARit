package com.onair.hearit.app.explore.presentation;

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.onair.hearit.app.auth.infrastructure.jwt.TokenStatus;
import com.onair.hearit.app.explore.application.HearitExploreService;
import com.onair.hearit.app.explore.dto.CursorResponseV2;
import com.onair.hearit.app.explore.dto.ExploredHearitResponse;
import com.onair.hearit.app.explore.dto.ExploredHearitResponse.KeywordResponse;
import com.onair.hearit.app.fixture.ControllerTest;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(controllers = ExploreController.class)
class ExploreControllerTest extends ControllerTest {

    @MockitoBean
    private HearitExploreService hearitExploreService;

    @Test
    @DisplayName("탐색 히어릿 목록 조회 V1 - 200 OK")
    void readExploredHearitsV1_OK() throws Exception {
        // given
        var responses = IntStream.range(1, 12).mapToObj(i -> new ExploredHearitResponse(
                        (long) i,
                        "Title " + i,
                        "#FFFFFF",
                        false,
                        null,
                        List.of(new KeywordResponse((long) i, "keyword1"), new KeywordResponse((long) i, "keyword2")),
                        (long) i - 1)
                )
                .toList();
        var pagedResponses = CursorResponseV2.from(responses);

        given(jwtTokenProvider.getTokenStatus("valid-token")).willReturn(TokenStatus.VALID);
        given(hearitExploreService.getExploredHearits(any(), any())).willReturn(pagedResponses);

        // when & then
        mockMvc.perform(get("/api/v1/hearits/explore")
                        .header("Authorization", "Bearer valid-token")
                        .param("cursorId", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andDo(MockMvcRestDocumentationWrapper.document("v1-get-hearits-explore-ok",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Explore API")
                                .summary("탐색 히어릿 목록 조회 V1")
                                .description("로그인한 사용자가 탐색된 히어릿 목록을 `cursorId` 기준으로 조회합니다.")
                                .queryParameters(
                                        parameterWithName("cursorId").description("시작 Cursor ID").defaultValue("0"),
                                        parameterWithName("size").description("필요한 히어릿 항목 수").defaultValue("10")
                                )
                                .responseFields(Stream.concat(
                                                Arrays.stream(new FieldDescriptor[]{
                                                        fieldWithPath("content[].id").description("히어릿 ID"),
                                                        fieldWithPath("content[].title").description("히어릿 제목"),
                                                        fieldWithPath("content[].categoryColorCode").description("카테고리 색상"),
                                                        fieldWithPath("content[].isBookmarked").description("북마크 여부"),
                                                        fieldWithPath("content[].bookmarkId").description("북마크 ID (북마크된 경우)").optional(),
                                                        fieldWithPath("content[].keywords").description("히어릿에 포함된 키워드 목록"),
                                                        fieldWithPath("content[].keywords[].id").description("키워드 ID"),
                                                        fieldWithPath("content[].keywords[].name").description("키워드 이름"),
                                                        fieldWithPath("content[].cursorId").description("커서 ID"),
                                                }),
                                                Arrays.stream(com.onair.hearit.fixture.ApiDocSnippets.getCustomCursorResponseV1Fields())
                                        ).toArray(FieldDescriptor[]::new)
                                )
                                .build())
                ));
    }

    @Test
    @DisplayName("탐색 히어릿 목록 조회 V2 - 200 OK")
    void readExploredHearitsV2_OK() throws Exception {
        // given
        var responses = IntStream.range(1, 12).mapToObj(i -> new ExploredHearitResponse(
                        (long) i,
                        "Title " + i,
                        "#FFFFFF",
                        false,
                        null,
                        List.of(new KeywordResponse((long) i, "keyword1"), new KeywordResponse((long) i, "keyword2")),
                        (long) i - 1)
                )
                .toList();
        var pagedResponses = CursorResponseV2.from(responses);

        given(jwtTokenProvider.getTokenStatus("valid-token")).willReturn(TokenStatus.VALID);
        given(hearitExploreService.getExploredHearits(any(), any())).willReturn(pagedResponses);

        // when & then
        mockMvc.perform(get("/api/v2/hearits/explore")
                        .header("Authorization", "Bearer valid-token")
                        .param("cursorId", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andDo(MockMvcRestDocumentationWrapper.document("v2-get-hearits-explore-ok",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Explore API")
                                .summary("탐색 히어릿 목록 조회 V2")
                                .description("로그인한 사용자가 탐색된 히어릿 목록을 `cursorId` 기준으로 조회합니다.")
                                .queryParameters(
                                        parameterWithName("cursorId").description("시작 Cursor ID").defaultValue("0"),
                                        parameterWithName("size").description("필요한 히어릿 항목 수").defaultValue("10")
                                )
                                .responseFields(Stream.concat(
                                                Arrays.stream(new FieldDescriptor[]{
                                                        fieldWithPath("content[].id").description("히어릿 ID"),
                                                        fieldWithPath("content[].title").description("히어릿 제목"),
                                                        fieldWithPath("content[].categoryColorCode").description("카테고리 색상"),
                                                        fieldWithPath("content[].isBookmarked").description("북마크 여부"),
                                                        fieldWithPath("content[].bookmarkId").description("북마크 ID (북마크된 경우)").optional(),
                                                        fieldWithPath("content[].keywords").description("히어릿에 포함된 키워드 목록"),
                                                        fieldWithPath("content[].keywords[].id").description("키워드 ID"),
                                                        fieldWithPath("content[].keywords[].name").description("키워드 이름"),
                                                        fieldWithPath("content[].cursorId").description("커서 ID"),
                                                }),
                                                Arrays.stream(com.onair.hearit.fixture.ApiDocSnippets.getCustomCursorResponseFields())
                                        ).toArray(FieldDescriptor[]::new)
                                )
                                .build())
                ));
    }
}
