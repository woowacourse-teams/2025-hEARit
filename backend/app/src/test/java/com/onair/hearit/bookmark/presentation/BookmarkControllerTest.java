package com.onair.hearit.bookmark.presentation;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.onair.hearit.auth.infrastructure.jwt.TokenStatus;
import com.onair.hearit.bookmark.application.BookmarkService;
import com.onair.hearit.bookmark.dto.BookmarkHearitResponseV2;
import com.onair.hearit.bookmark.dto.BookmarkHearitResponseV2.CategoryResponse;
import com.onair.hearit.bookmark.dto.BookmarkHearitResponseV2.SourceResponse;
import com.onair.hearit.bookmark.dto.BookmarkInfoResponse;
import com.onair.hearit.exception.custom.AlreadyExistException;
import com.onair.hearit.exception.custom.ForbiddenException;
import com.onair.hearit.fixture.ApiDocSnippets;
import com.onair.hearit.fixture.ControllerTest;
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

@WebMvcTest(controllers = BookmarkController.class)
class BookmarkControllerTest extends ControllerTest {

    @MockitoBean
    private BookmarkService bookmarkService;

    @Test
    @DisplayName("북마크 목록 조회 V1 - 200 OK")
    void readBookmarkHearitsV1_OK() throws Exception {
        // given
        var responses = IntStream.range(1, 25).mapToObj(i -> new BookmarkHearitResponseV2(
                        (long) i,
                        (long) (1000 + i),
                        "Title " + i,
                        "Summary " + i + 1,
                        100 + i,
                        (long) 200 + i - 2,
                        false,
                        List.of(new SourceResponse("source1", "url1"), new SourceResponse("source2", "url2")),
                        new CategoryResponse((long) i, "categoryName", "#FFFFFF")
                ))
                .toList();
        var pagedResponses = new PageImpl<>(responses, PageRequest.of(0, 20), responses.size());

        given(jwtTokenProvider.getTokenStatus("valid-token")).willReturn(TokenStatus.VALID);
        given(bookmarkService.getBookmarkHearits(any(), any())).willReturn(pagedResponses);

        // when & then
        mockMvc.perform(get("/api/v1/bookmarks/hearits")
                        .header("Authorization", "Bearer valid-token")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andDo(document("v1-get-bookmarks-hearits-ok",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Bookmark API")
                                .summary("북마크 목록 조회 V1")
                                .description("로그인한 사용자가 북마크한 히어릿 목록을 `Page` 단위로 조회합니다.")
                                .queryParameters(
                                        parameterWithName("page").description("페이지 번호 (start 0)").defaultValue("0"),
                                        parameterWithName("size").description("페이지 당 항목 수").defaultValue("20")
                                )
                                .responseFields(Stream.concat(
                                                Arrays.stream(new FieldDescriptor[]{
                                                        fieldWithPath("content[].hearitId").description("히어릿 ID"),
                                                        fieldWithPath("content[].bookmarkId").description("북마크 ID"),
                                                        fieldWithPath("content[].title").description("히어릿 제목"),
                                                        fieldWithPath("content[].summary").description("히어릿 요약"),
                                                        fieldWithPath("content[].playTime").description("히어릿 재생 시간(초)"),
                                                        fieldWithPath("content[].lastPlayTime").description("히어릿 마지막 재생 시간(ms)").optional(),
                                                        fieldWithPath("content[].categoryColor").description("카테고리 색상 코드")
                                                }), Arrays.stream(ApiDocSnippets.getCustomPagedResponseFields()))
                                        .toArray(FieldDescriptor[]::new)
                                )
                                .build())
                ));
    }

    @Test
    @DisplayName("북마크 목록 조회 V2 - 200 OK")
    void readBookmarkHearitsV2_OK() throws Exception {
        // given
        var responses = IntStream.range(1, 25).mapToObj(i -> new BookmarkHearitResponseV2(
                        (long) i,
                        (long) (1000 + i),
                        "Title " + i,
                        "Summary " + i + 1,
                        100 + i,
                        (long) 200 + i - 2,
                        false,
                        List.of(new SourceResponse("source1", "url1"), new SourceResponse("source2", "url2")),
                        new CategoryResponse((long) i, "categoryName", "#FFFFFF")
                ))
                .toList();
        var pagedResponses = new PageImpl<>(responses, PageRequest.of(0, 20), responses.size());

        given(jwtTokenProvider.getTokenStatus("valid-token")).willReturn(TokenStatus.VALID);
        given(bookmarkService.getBookmarkHearits(any(), any())).willReturn(pagedResponses);

        // when & then
        mockMvc.perform(get("/api/v2/bookmarks/hearits")
                        .header("Authorization", "Bearer valid-token")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andDo(document("v2-get-bookmarks-hearits-ok",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Bookmark API")
                                .summary("북마크 목록 조회 V2")
                                .description("로그인한 사용자가 북마크한 히어릿 목록을 `Page` 단위로 조회합니다.")
                                .queryParameters(
                                        parameterWithName("page").description("페이지 번호 (start 0)").defaultValue("0"),
                                        parameterWithName("size").description("페이지 당 항목 수").defaultValue("20")
                                )
                                .responseFields(Stream.concat(
                                                Arrays.stream(new FieldDescriptor[]{
                                                        fieldWithPath("content[].hearitId").description("히어릿 ID"),
                                                        fieldWithPath("content[].bookmarkId").description("북마크 ID"),
                                                        fieldWithPath("content[].title").description("히어릿 제목"),
                                                        fieldWithPath("content[].summary").description("히어릿 요약"),
                                                        fieldWithPath("content[].playTime").description("히어릿 재생 시간(초)"),
                                                        fieldWithPath("content[].lastPlayTime").description("히어릿 마지막 재생 시간(ms)").optional(),
                                                        fieldWithPath("content[].isFinished").description("히어릿 재생 완료 여부").optional(),
                                                        fieldWithPath("content[].sources").description("출처 정보"),
                                                        fieldWithPath("content[].sources[].sourceName").description("출처 이름"),
                                                        fieldWithPath("content[].sources[].sourceUrl").description("출처 URL"),
                                                        fieldWithPath("content[].category").description("카테고리 정보"),
                                                        fieldWithPath("content[].category.id").description("카테고리 ID"),
                                                        fieldWithPath("content[].category.name").description("카테고리 이름"),
                                                        fieldWithPath("content[].category.colorCode").description("카테고리 색상코드")
                                                }), Arrays.stream(ApiDocSnippets.getCustomPagedResponseFields()))
                                        .toArray(FieldDescriptor[]::new)
                                )
                                .build())
                ));
    }

    @Test
    @DisplayName("북마크 목록 조회 V2 - 400 BadRequest")
    void readBookmarkHearitsV2_BadRequest() throws Exception {
        // given
        given(jwtTokenProvider.getTokenStatus("valid-token")).willReturn(TokenStatus.VALID);

        // when & then
        mockMvc.perform(get("/api/v2/bookmarks/hearits")
                        .header("Authorization", "Bearer valid-token")
                        .param("page", "-1")
                        .param("size", "20"))
                .andExpect(status().isBadRequest())
                .andDo(document("v2-get-bookmarks-hearits-bad-request",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Bookmark API")
                                .summary("북마크 목록 조회 V2")
                                .responseFields(ApiDocSnippets.getProblemDetailResponseFields())
                                .build())
                ));
    }

    @Test
    @DisplayName("북마크 목록 조회 V2 - 401 Unauthorized")
    void readBookmarkHearitsV2_Unauthorized() throws Exception {
        // given
        given(jwtTokenProvider.getTokenStatus(isNull())).willReturn(TokenStatus.NOT_EXIST);

        // when & then
        mockMvc.perform(get("/api/v2/bookmarks/hearits")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isUnauthorized())
                .andDo(document("v2-get-bookmarks-hearits-unauthorized",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Bookmark API")
                                .summary("북마크 목록 조회 V2")
                                .responseFields(ApiDocSnippets.getProblemDetailResponseFieldsWithAuthProperties())
                                .build())
                ));
    }

    @Test
    @DisplayName("북마크 추가 V1 - 201 Created")
    void createBookmarkV1_Created() throws Exception {
        // given
        var hearitId = 3L;
        var response = new BookmarkInfoResponse(hearitId);

        given(jwtTokenProvider.getTokenStatus("valid-token")).willReturn(TokenStatus.VALID);
        given(bookmarkService.addBookmark(any(), any())).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/bookmarks/hearits/{hearitId}", hearitId)
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isCreated())
                .andDo(document("v1-post-bookmarks-hearits-created",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Bookmark API")
                                .summary("북마크 생성 V1")
                                .description("로그인한 사용자가 히어릿에 대한 북마크를 생성합니다.")
                                .pathParameters(
                                        parameterWithName("hearitId").description("북마크 대상 히어릿 ID")
                                )
                                .responseFields(
                                        fieldWithPath("id").description("생성된 북마크 ID")
                                )
                                .build())
                ));
    }

    @Test
    @DisplayName("북마크 추가 V1 - 409 Conflict")
    void createBookmarkV1_Conflict() throws Exception {
        // given
        var hearitId = 3L;

        given(jwtTokenProvider.getTokenStatus("valid-token")).willReturn(TokenStatus.VALID);
        given(bookmarkService.addBookmark(any(), any()))
                .willThrow(new AlreadyExistException("이미 북마크된 히어릿입니다."));

        // when & then
        mockMvc.perform(post("/api/v1/bookmarks/hearits/{hearitId}", hearitId)
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isConflict())
                .andDo(document("v1-post-bookmarks-hearits-conflict",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Bookmark API")
                                .summary("북마크 생성 V1")
                                .responseFields(ApiDocSnippets.getProblemDetailResponseFields())
                                .build())
                ));
    }

    @Test
    @DisplayName("북마크 삭제 V1 - 204 No Content")
    void deleteBookmarkV1_NoContent() throws Exception {
        // given
        var bookmarkId = 1L;

        given(jwtTokenProvider.getTokenStatus("valid-token")).willReturn(TokenStatus.VALID);
        willDoNothing().given(bookmarkService).deleteBookmark(any(), any());

        // when & then
        mockMvc.perform(delete("/api/v1/bookmarks/{bookmarkId}", bookmarkId)
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isNoContent())
                .andDo(document("v1-delete-bookmarks-no-content",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Bookmark API")
                                .summary("북마크 삭제 V1")
                                .description("로그인한 사용자가 히어릿에 대한 북마크를 삭제합니다.")
                                .pathParameters(
                                        parameterWithName("bookmarkId").description("삭제 대상 북마크 ID")
                                )
                                .build())
                ));
    }

    @Test
    @DisplayName("북마크 삭제 V1 - 403 Forbidden")
    void deleteBookmarkV1_Forbidden() throws Exception {
        // given
        var bookmarkId = 1L;

        given(jwtTokenProvider.getTokenStatus("valid-token")).willReturn(TokenStatus.VALID);
        willThrow(new ForbiddenException("북마크를 삭제할 권한이 없습니다."))
                .given(bookmarkService).deleteBookmark(any(), any());

        // when & then
        mockMvc.perform(delete("/api/v1/bookmarks/{bookmarkId}", bookmarkId)
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isForbidden())
                .andDo(document("v1-delete-bookmarks-forbidden",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Bookmark API")
                                .summary("북마크 삭제 V1")
                                .responseFields(ApiDocSnippets.getProblemDetailResponseFields())
                                .build())
                ));
    }
}
