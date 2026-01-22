package com.onair.hearit.app.member.presentation;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.onair.hearit.app.auth.infrastructure.jwt.TokenStatus;
import com.onair.hearit.app.fixture.ControllerTest;
import com.onair.hearit.app.member.application.MemberService;
import com.onair.hearit.app.member.dto.MemberInfoResponse;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(controllers = MemberController.class)
class MemberControllerTest extends ControllerTest {

    @MockitoBean
    private MemberService memberService;

    @Test
    @DisplayName("사용자 정보 조회 V1 - 200 OK")
    void getMemberInfoV1_OK() throws Exception {
        // given
        var response = new MemberInfoResponse(
                1L,
                "nickname",
                "profile-image.jpg"
        );

        given(jwtTokenProvider.getTokenStatus("valid-token")).willReturn(TokenStatus.VALID);
        given(jwtTokenProvider.getMemberUuid("valid-token")).willReturn(UUID.randomUUID());
        given(memberService.getMember(any())).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/members/me")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andDo(document("v1-get-members-me-ok",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Member API")
                                .summary("사용자 정보 조회 V1")
                                .description("현재 로그인한 사용자의 정보를 조회합니다.")
                                .responseFields(
                                        fieldWithPath("id").description("사용자 ID"),
                                        fieldWithPath("nickname").description("닉네임"),
                                        fieldWithPath("profileImage").description("프로필 이미지 URL")
                                )
                                .build())
                ));
    }

    @Test
    @DisplayName("사용자 정보 조회 V1 - 401 Unauthorized")
    void getMemberInfoV1_Unauthorized() throws Exception {
        // given
        given(jwtTokenProvider.getTokenStatus(isNull())).willReturn(TokenStatus.NOT_EXIST);

        // when & then
        mockMvc.perform(get("/api/v1/members/me"))
                .andExpect(status().isUnauthorized())
                .andDo(document("v1-get-members-me-unauthorized",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Member API")
                                .responseFields(
                                        com.onair.hearit.fixture.ApiDocSnippets.getProblemDetailResponseFieldsWithAuthProperties())
                                .build())
                ));
    }
}
