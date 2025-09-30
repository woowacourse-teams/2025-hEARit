package com.onair.hearit.playinghistory.presentation;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onair.hearit.auth.infrastructure.jwt.TokenStatus;
import com.onair.hearit.fixture.ControllerTest;
import com.onair.hearit.playinghistory.application.PlayingHistoryService;
import com.onair.hearit.playinghistory.dto.PlayingHistoryRequest;
import com.onair.hearit.playinghistory.dto.RecentlyPlayedHearitResponse;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(controllers = PlayingHistoryController.class)
class PlayingHistoryControllerTest extends ControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PlayingHistoryService playingHistoryService;

    @Test
    @DisplayName("최근 재생 기록 조회 V1 - 로그인 사용자 200 OK")
    void getRecentPlayingHistoriesWhenMemberV1_OK() throws Exception {
        // given
        var responses = List.of(
                new RecentlyPlayedHearitResponse(1L, "title1", 120, 30L, null,
                        new RecentlyPlayedHearitResponse.CategoryResponse(1L, "카테고리1", "#000000")),
                new RecentlyPlayedHearitResponse(2L, "title2", 150, 60L, null,
                        new RecentlyPlayedHearitResponse.CategoryResponse(2L, "카테고리2", "#111111"))
        );

        given(jwtTokenProvider.getTokenStatus("valid-token")).willReturn(TokenStatus.VALID);
        given(playingHistoryService.getRecentPlayingHistory(any())).willReturn(responses);

        // when & then
        mockMvc.perform(get("/api/v1/playing-histories/hearits")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andDo(document("v1-get-playing-histories-member-ok",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Playing History API")
                                .summary("최근 재생 기록 조회 V1")
                                .description("로그인한 사용자는 최근 재생 기록을 최대 10개까지 조회합니다.")
                                .responseFields(
                                        fieldWithPath("[].id").description("히어릿 ID"),
                                        fieldWithPath("[].title").description("히어릿 제목"),
                                        fieldWithPath("[].playTime").description("히어릿 전체 재생 시간(s)"),
                                        fieldWithPath("[].lastPlayTime").description("사용자가 마지막으로 재생한 시간(ms)"),
                                        fieldWithPath("[].createdAt").description("히어릿 생성일").optional(),
                                        fieldWithPath("[].category.id").description("카테고리 ID"),
                                        fieldWithPath("[].category.name").description("카테고리 이름"),
                                        fieldWithPath("[].category.colorCode").description("카테고리 색상 코드")
                                )
                                .build())
                ));
    }

    @Test
    @DisplayName("최근 재생 기록 조회 V1 - 게스트 사용자 200 OK 빈 리스트")
    void getRecentPlayingHistoriesWhenGuestV1_OK() throws Exception {
        // given
        given(jwtTokenProvider.getTokenStatus(isNull())).willReturn(TokenStatus.NOT_EXIST);
        given(playingHistoryService.getRecentPlayingHistory(any())).willReturn(List.of());

        // when & then
        mockMvc.perform(get("/api/v1/playing-histories/hearits"))
                .andExpect(status().isOk())
                .andDo(document("v1-get-playing-histories-guest-ok",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Playing History API")
                                .summary("최근 재생 기록 조회 V1")
                                .description("로그인하지 않은 사용자는 빈 리스트를 반환합니다.")
                                .responseFields(
                                        fieldWithPath("[]").description("빈 리스트")
                                )
                                .build())
                ));
    }

    @Test
    @DisplayName("재생기록 생성/수정 V1 - 사용자 200 OK")
    void createPlayingHistoryWhenMemberV1_OK() throws Exception {
        // given
        var request = new PlayingHistoryRequest(1L, 100L);

        given(jwtTokenProvider.getTokenStatus("valid-token")).willReturn(TokenStatus.VALID);
        willDoNothing().given(playingHistoryService).addPlayingHistory(any(), any());

        // when & then
        mockMvc.perform(post("/api/v1/playing-histories")
                        .header("Authorization", "Bearer valid-token")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("v1-post-playing-history-member-ok",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Playing History API")
                                .summary("재생기록 생성/수정 V1")
                                .description("사용자의 재생 기록을 생성하거나 업데이트합니다.")
                                .requestFields(
                                        fieldWithPath("hearitId").description("히어릿 ID"),
                                        fieldWithPath("lastPlayTime").description("마지막 재생 시간(ms)")
                                )
                                .build())
                ));
    }

    @Test
    @DisplayName("재생기록 생성/수정 V1 - 게스트 200 OK")
    void createPlayingHistoryWhenGuestV1_OK() throws Exception {
        // given
        var request = new PlayingHistoryRequest(1L, 100L);

        given(jwtTokenProvider.getTokenStatus(isNull())).willReturn(TokenStatus.NOT_EXIST);
        willDoNothing().given(playingHistoryService).addPlayingHistory(any(), any());

        // when & then
        mockMvc.perform(post("/api/v1/playing-histories")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("v1-post-playing-history-guest-ok",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Playing History API")
                                .summary("재생기록 생성/수정 V1")
                                .description("로그인하지 않은 사용자는 재생 기록을 저장하지 않습니다.")
                                .requestFields(
                                        fieldWithPath("hearitId").description("히어릿 ID"),
                                        fieldWithPath("lastPlayTime").description("마지막 재생 시간(ms)")
                                )
                                .build())
                ));
    }
}
