package com.onair.hearit.app.search.presentation;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
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
import com.onair.hearit.app.common.dto.response.PagedResponse;
import com.onair.hearit.app.fixture.ControllerTest;
import com.onair.hearit.app.search.application.HearitSearchService;
import com.onair.hearit.app.search.dto.HearitSearchResponse;
import com.onair.hearit.app.search.dto.HearitSearchResponse.KeywordResponse;
import com.onair.hearit.app.search.dto.SearchAutocompleteResponse;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(controllers = HearitSearchController.class)
public class HearitSearchControllerTest extends ControllerTest {

    @MockitoBean
    private HearitSearchService hearitSearchService;

    @Test
    @DisplayName("히어릿 검색 V1 - 200 OK")
    void readSearchedHearitsV1_OK() throws Exception {
        // given
        var responses = List.of(
                new HearitSearchResponse(1L, "examplespring1", 300, null, false,
                        List.of(new KeywordResponse(1L, "spring"), new KeywordResponse(2L, "boot"))),
                new HearitSearchResponse(2L, "SPRING1example", 200, null, false,
                        List.of(new KeywordResponse(2L, "noKeyword"))),
                new HearitSearchResponse(3L, "notitle", 400, null, false,
                        List.of(new KeywordResponse(1L, "Spring"))));
        var pagedResponses = PagedResponse.from(new PageImpl<>(responses, PageRequest.of(0, 20), responses.size()));

        given(jwtTokenProvider.getTokenStatus("valid-token")).willReturn(TokenStatus.VALID);
        given(jwtTokenProvider.getMemberUuid("valid-token")).willReturn(UUID.randomUUID());
        given(hearitSearchService.search(any(), any(), any())).willReturn(pagedResponses);

        // when & then
        mockMvc.perform(get("/api/v1/hearits/search")
                        .header("Authorization", "Bearer valid-token")
                        .param("searchTerm", "spring")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andDo(document("v1-get-hearits-search-ok",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Search API")
                                .summary("히어릿 검색 V1")
                                .description("제목 또는 키워드에 검색어가 포함된 히어릿 목록을 페이지네이션 방식으로 조회합니다.")
                                .queryParameters(
                                        parameterWithName("searchTerm").description("검색어"),
                                        parameterWithName("page").description("페이지 번호 (0부터 시작)").defaultValue("0"),
                                        parameterWithName("size").description("페이지 당 항목 수 (기본 20)").defaultValue("20")
                                )
                                .responseFields(
                                        Stream.concat(
                                                Arrays.stream(new FieldDescriptor[]{
                                                        fieldWithPath("content[].id").description("히어릿 ID"),
                                                        fieldWithPath("content[].title").description("히어릿 제목"),
                                                        fieldWithPath("content[].playTime").description("히어릿 재생 시간(초)"),
                                                        fieldWithPath("content[].lastPlayTime").description(
                                                                "히어릿 마지막 재생 시간(ms)").optional(),
                                                        fieldWithPath("content[].isFinished").description(
                                                                "히어릿을 끝까지 시청했는지 여부").optional(),
                                                        fieldWithPath("content[].keywords").description(
                                                                "히어릿에 포함된 키워드 목록"),
                                                        fieldWithPath("content[].keywords[].id").description("키워드 ID"),
                                                        fieldWithPath("content[].keywords[].name").description("키워드 이름")
                                                }),
                                                Arrays.stream(
                                                        com.onair.hearit.fixture.ApiDocSnippets.getCustomPagedResponseFields())
                                        ).toArray(FieldDescriptor[]::new)
                                )
                                .build())
                ));
    }

    @Test
    @DisplayName("히어릿 검색 V1 - 400 Bad Request")
    void readSearchedHearitsV1_BadRequest() throws Exception {
        // given
        given(jwtTokenProvider.getTokenStatus("valid-token")).willReturn(TokenStatus.VALID);
        given(jwtTokenProvider.getMemberUuid("valid-token")).willReturn(java.util.UUID.randomUUID());

        // when & then
        mockMvc.perform(get("/api/v1/hearits/search")
                        .header("Authorization", "Bearer valid-token")
                        .param("searchTerm", "spring")
                        .param("page", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest())
                .andDo(document("v1-get-hearits-search-bad-request",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Search API")
                                .summary("히어릿 검색 V1")
                                .responseFields(
                                        com.onair.hearit.fixture.ApiDocSnippets.getProblemDetailResponseFields())
                                .build())
                ));
    }

    @Test
    @DisplayName("히어릿 검색 V2 - 200 OK")
    void readSearchedHearitsV2_OK() throws Exception {
        // given
        var responses = List.of(
                new HearitSearchResponse(1L, "examplespring1", 300, null, false,
                        List.of(new KeywordResponse(1L, "spring"), new KeywordResponse(2L, "boot"))),
                new HearitSearchResponse(2L, "SPRING1example", 200, null, false,
                        List.of(new KeywordResponse(2L, "noKeyword"))),
                new HearitSearchResponse(3L, "notitle", 400, null, false,
                        List.of(new KeywordResponse(1L, "Spring"))));
        var pagedResponses = PagedResponse.from(new PageImpl<>(responses, PageRequest.of(0, 20), responses.size()));

        given(jwtTokenProvider.getTokenStatus("valid-token")).willReturn(TokenStatus.VALID);
        given(jwtTokenProvider.getMemberUuid("valid-token")).willReturn(UUID.randomUUID());
        given(hearitSearchService.searchV2(any(), any(), any(), any())).willReturn(pagedResponses);

        // when & then
        mockMvc.perform(get("/api/v2/hearits/search")
                        .header("Authorization", "Bearer valid-token")
                        .param("searchTerm", "spring")
                        .param("sort", "recommend")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andDo(document("v2-get-hearits-search-ok",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Search API")
                                .summary("히어릿 검색 V2")
                                .description("제목 또는 키워드에 검색어가 포함된 히어릿 목록을 페이지네이션 방식으로 조회합니다.")
                                .queryParameters(
                                        parameterWithName("searchTerm").description("검색어"),
                                        parameterWithName("sort").description(
                                                        "검색 정렬 기준 (추천순: recommend| 최신순: latest| 오래된 순: oldest| 정확도 순: accuracy)")
                                                .defaultValue("recommend"),
                                        parameterWithName("page").description("페이지 번호 (0부터 시작)").defaultValue("0"),
                                        parameterWithName("size").description("페이지 당 항목 수").defaultValue("20")
                                )
                                .responseFields(
                                        Stream.concat(
                                                Arrays.stream(new FieldDescriptor[]{
                                                        fieldWithPath("content[].id").description("히어릿 ID"),
                                                        fieldWithPath("content[].title").description("히어릿 제목"),
                                                        fieldWithPath("content[].playTime").description("히어릿 재생 시간(초)"),
                                                        fieldWithPath("content[].lastPlayTime").description(
                                                                "히어릿 마지막 재생 시간(ms)").optional(),
                                                        fieldWithPath("content[].isFinished").description(
                                                                "히어릿을 끝까지 시청했는지 여부").optional(),
                                                        fieldWithPath("content[].keywords").description(
                                                                "히어릿에 포함된 키워드 목록"),
                                                        fieldWithPath("content[].keywords[].id").description("키워드 ID"),
                                                        fieldWithPath("content[].keywords[].name").description("키워드 이름")
                                                }),
                                                Arrays.stream(
                                                        com.onair.hearit.fixture.ApiDocSnippets.getCustomPagedResponseFields())
                                        ).toArray(FieldDescriptor[]::new)
                                )
                                .build())));
    }

    @Test
    @DisplayName("자동완성 검색 - 200 OK")
    void readSearchedAutocomplete_OK() throws Exception {
        // given
        given(hearitSearchService.getAutocomplete(any(), anyInt()))
                .willReturn(new SearchAutocompleteResponse(List.of("Spring", "Spring Boot")));

        // when & then
        mockMvc.perform(get("/api/v1/hearits/search/autocomplete")
                        .param("searchTerm", "spring")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andDo(document("v1-get-hearits-search-autocomplete-ok",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Search API")
                                .summary("자동완성 검색")
                                .description("검색어에 대한 자동완성 결과를 반환합니다.")
                                .queryParameters(
                                        parameterWithName("searchTerm").description("검색어"),
                                        parameterWithName("size").description("자동완성 결과 수 (1~30, 기본 5)").defaultValue("5")
                                )
                                .responseFields(
                                        fieldWithPath("autocompletes").description("자동완성 결과 목록")
                                )
                                .build())));
    }

    @Test
    @DisplayName("자동완성 검색 - 400 Bad Request")
    void readSearchedAutocomplete_BadRequest() throws Exception {
        // size below min (1)
        mockMvc.perform(get("/api/v1/hearits/search/autocomplete")
                        .param("searchTerm", "spring")
                        .param("size", "0"))
                .andExpect(status().isBadRequest())
                .andDo(document("v1-get-hearits-search-autocomplete-bad-request",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Search API")
                                .summary("자동완성 검색")
                                .responseFields(
                                        com.onair.hearit.fixture.ApiDocSnippets.getProblemDetailResponseFields())
                                .build())));

        // size above max (30)
        mockMvc.perform(get("/api/v1/hearits/search/autocomplete")
                        .param("searchTerm", "spring")
                        .param("size", "31"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("히어릿 검색 V2 - 400 Bad Request")
    void readSearchedHearitsV2_BadRequest() throws Exception {
        // given
        given(jwtTokenProvider.getTokenStatus("valid-token")).willReturn(TokenStatus.VALID);
        given(jwtTokenProvider.getMemberUuid("valid-token")).willReturn(java.util.UUID.randomUUID());

        // when & then
        mockMvc.perform(get("/api/v2/hearits/search")
                        .header("Authorization", "Bearer valid-token")
                        .param("searchTerm", "spring")
                        .param("sort", "recommend")
                        .param("page", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest())
                .andDo(document("v2-get-hearits-search-bad-request",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Search API")
                                .summary("히어릿 검색 V2")
                                .responseFields(
                                        com.onair.hearit.fixture.ApiDocSnippets.getProblemDetailResponseFields())
                                .build())));
    }
}
