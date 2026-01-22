package com.onair.hearit.app.like.presentation;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.onair.hearit.app.auth.infrastructure.jwt.TokenStatus;
import com.onair.hearit.app.exception.custom.ForbiddenException;
import com.onair.hearit.app.fixture.ControllerTest;
import com.onair.hearit.app.like.application.LikeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(controllers = LikeController.class)
class LikeControllerTest extends ControllerTest {

    @MockitoBean
    private LikeService likeService;

    @Test
    @DisplayName("204 NoContent - 좋아요 삭제")
    void deleteLike_NoContent() throws Exception {
        // given
        var hearitId = 1L;

        given(jwtTokenProvider.getTokenStatus("valid-token")).willReturn(TokenStatus.VALID);
        given(jwtTokenProvider.getMemberUuid("valid-token")).willReturn(java.util.UUID.randomUUID());

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/hearits/{hearitId}/likes", hearitId)
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isNoContent())
                .andDo(document("v1-delete-like",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Like API")
                                .summary("좋아요 삭제 V1")
                                .description("회원이 좋아요를 삭제합니다.")
                                .pathParameters(
                                        parameterWithName("hearitId").description("좋아요 대상 히어릿 ID"))
                                .build())
                ));

    }

    @Test
    @DisplayName("403 Forbidden - 좋아요 삭제 권한 없음")
    void deleteLike_Forbidden() throws Exception {
        // given
        var hearitId = 1L;

        willThrow(new ForbiddenException("비회원은 좋아요을 삭제할 권한이 없습니다."))
                .given(likeService).removeLike(any(), any());

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/hearits/{hearitId}/likes", hearitId))
                .andExpect(status().isForbidden())
                .andDo(document("v1-delete-like-forbidden",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Like API")
                                .summary("좋아요 삭제 V1")
                                .description("회원이 좋아요를 삭제합니다.")
                                .pathParameters(
                                        parameterWithName("hearitId").description("좋아요 대상 히어릿 ID"))
                                .responseFields(
                                        com.onair.hearit.fixture.ApiDocSnippets.getProblemDetailResponseFields())
                                .build())
                ));
    }

    @Test
    @DisplayName("201 Created - 좋아요 추가")
    void createLike_Created() throws Exception {
        // given
        var hearitId = 1L;

        given(jwtTokenProvider.getTokenStatus("valid-token")).willReturn(TokenStatus.VALID);
        given(jwtTokenProvider.getMemberUuid("valid-token")).willReturn(java.util.UUID.randomUUID());

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/hearits/{hearitId}/likes", hearitId)
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isCreated())
                .andDo(document("v1-post-like",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Like API")
                                .summary("좋아요 추가 V1")
                                .description("회원이 좋아요를 추가합니다.")
                                .pathParameters(
                                        parameterWithName("hearitId").description("좋아요 대상 히어릿 ID"))
                                .build())
                ));

    }

    @Test
    @DisplayName("403 Forbidden - 좋아요 추가 권한 없음")
    void addLike_Forbidden() throws Exception {
        // given
        var hearitId = 1L;

        willThrow(new ForbiddenException("비회원은 좋아요을 추가할 권한이 없습니다."))
                .given(likeService).addLike(any(), any());

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/hearits/{hearitId}/likes", hearitId))
                .andExpect(status().isForbidden())
                .andDo(document("v1-post-like-forbidden",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Like API")
                                .summary("좋아요 추가 V1")
                                .description("회원이 좋아요를 추가합니다.")
                                .pathParameters(
                                        parameterWithName("hearitId").description("좋아요 대상 히어릿 ID"))
                                .responseFields(
                                        com.onair.hearit.fixture.ApiDocSnippets.getProblemDetailResponseFields())
                                .build())
                ));
    }
}
