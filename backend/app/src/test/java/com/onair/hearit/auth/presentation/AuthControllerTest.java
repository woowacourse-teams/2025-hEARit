package com.onair.hearit.auth.presentation;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onair.hearit.auth.application.AuthService;
import com.onair.hearit.auth.dto.request.LoginRequest;
import com.onair.hearit.auth.dto.request.OAuthLoginRequest;
import com.onair.hearit.auth.dto.request.SignupRequest;
import com.onair.hearit.auth.dto.request.TokenReissueRequest;
import com.onair.hearit.auth.dto.response.LoginTokenResponse;
import com.onair.hearit.auth.dto.response.TokenReissueResponse;
import com.onair.hearit.auth.infrastructure.jwt.TokenStatus;
import com.onair.hearit.exception.custom.InvalidInputException;
import com.onair.hearit.exception.custom.UnauthorizedException;
import com.onair.hearit.fixture.ApiDocSnippets;
import com.onair.hearit.fixture.ControllerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(controllers = AuthController.class)
class AuthControllerTest extends ControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Test
    @DisplayName("로컬 로그인 V1 - 200 OK")
    void loginV1_OK() throws Exception {
        // given
        var request = new LoginRequest("test123", "password1234");
        var response = new LoginTokenResponse(
                "access12341234.token12341234.fake-signature",
                "refresh12341234.token12341234.fake-signature"
        );

        given(authService.login(any())).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("v1-post-auth-login-ok",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Auth API")
                                .summary("로컬 로그인 V1")
                                .description("아이디/비밀번호로 로그인하여 `accessToken`과 `refreshToken`을 발급받습니다.")
                                .requestFields(
                                        fieldWithPath("localId").description("로컬 아이디"),
                                        fieldWithPath("password").description("로컬 비밀번호")
                                )
                                .responseFields(
                                        fieldWithPath("accessToken").description("발급된 access token"),
                                        fieldWithPath("refreshToken").description("발급된 refresh token")
                                )
                                .build())
                ));
    }

    @Test
    @DisplayName("로컬 로그인 V1 - 401 Unauthorized")
    void loginV1_Unauthorized() throws Exception {
        // given
        var request = new LoginRequest("test123", "wrong-pass");

        given(authService.login(any()))
                .willThrow(new UnauthorizedException("아이디나 비밀번호가 일치하지 않습니다."));

        // when & then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andDo(document("v1-post-auth-login-unauthorized",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Auth API")
                                .summary("로컬 로그인 V1")
                                .responseFields(ApiDocSnippets.getProblemDetailResponseFields())
                                .build())
                ));
    }

    @Test
    @DisplayName("카카오 로그인 V1 - 200 OK")
    void kakaoLoginV1_OK() throws Exception {
        // given
        var request = new OAuthLoginRequest("kakao-access-token-12345");
        var response = new LoginTokenResponse(
                "access12341234.token12341234.fake-signature",
                "refresh12341234.token12341234.fake-signature"
        );

        given(authService.loginOrSignUp(any(), any())).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/auth/kakao-login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("v1-post-auth-kakao-login-ok",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Auth API")
                                .summary("카카오 로그인 V1")
                                .description(
                                        "카카오 `accessToken`으로 로그인 또는 회원가입을 진행하여 `accessToken`과 `refreshToken`을 발급받습니다.")
                                .requestFields(
                                        fieldWithPath("accessToken").description("카카오 access token")
                                )
                                .responseFields(
                                        fieldWithPath("accessToken").description("발급된 access token"),
                                        fieldWithPath("refreshToken").description("발급된 refresh token")
                                )
                                .build())
                ));
    }

    @Test
    @DisplayName("카카오 로그인 V1 - 401 Unauthorized")
    void kakaoLoginV1_Unauthorized() throws Exception {
        // given
        var request = new OAuthLoginRequest("invalid-kakao-access-token");

        given(authService.loginOrSignUp(any(), any()))
                .willThrow(new UnauthorizedException("this access token is already expired"));

        // when & then
        mockMvc.perform(post("/api/v1/auth/kakao-login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andDo(document("v1-post-auth-kakao-login-unauthorized",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Auth API")
                                .summary("카카오 로그인 V1")
                                .responseFields(ApiDocSnippets.getProblemDetailResponseFields())
                                .build())
                ));
    }

    @Test
    @DisplayName("로컬 회원가입 V1 - 201 Created")
    void signupV1_Created() throws Exception {
        // given
        var request = new SignupRequest("newUser123", "newNickname", "password1234");

        willDoNothing().given(authService).signup(any());

        // when & then
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andDo(document("v1-post-auth-signup-created",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Auth API")
                                .summary("로컬 회원가입 V1")
                                .description("아이디/비밀번호를 통해 새로운 계정을 생성합니다.")
                                .requestFields(
                                        fieldWithPath("localId").description("회원 아이디"),
                                        fieldWithPath("nickname").description("닉네임"),
                                        fieldWithPath("password").description("비밀번호")
                                )
                                .build())
                ));
    }

    @Test
    @DisplayName("로컬 회원가입 V1 - 400 Bad Request")
    void signupV1_BadRequest() throws Exception {
        // given
        var request = new SignupRequest("existingUser", "newNickname", "password1234");

        willThrow(new InvalidInputException("이미 존재하는 아이디입니다."))
                .given(authService).signup(any());

        // when & then
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(document("v1-post-auth-signup-bad-request",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Auth API")
                                .summary("로컬 회원가입 V1")
                                .responseFields(ApiDocSnippets.getProblemDetailResponseFields())
                                .build())
                ));
    }

    @DisplayName("access token 재발급 V1 - 200 OK")
    @Test
    void refresh_tokenV1_OK() throws Exception {
        // given
        var request = new TokenReissueRequest("refresh12341234.token12341234.fake-signature");
        var response = new TokenReissueResponse("new-access12341234.token12341234.fake-signature");

        given(authService.reissue(any())).willReturn(response.accessToken());

        // when & then
        mockMvc.perform(post("/api/v1/auth/token/refresh")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("v1-post-auth-token-refresh-ok",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Auth API")
                                .summary("access token 재발급 V1")
                                .description("`refreshToken`으로 `accessToken`을 재발급 받습니다.")
                                .requestFields(
                                        fieldWithPath("refreshToken").description("refresh token")
                                )
                                .responseFields(
                                        fieldWithPath("accessToken").description("발급된 access token")
                                )
                                .build())
                ));
    }

    @Test
    @DisplayName("access token 검증 V1 - 200 OK")
    void checkV1_OK() throws Exception {
        // given
        given(jwtTokenProvider.getTokenStatus("valid-token")).willReturn(TokenStatus.VALID);

        // when & then
        mockMvc.perform(get("/api/v1/auth/check")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andDo(document("v1-get-auth-check-ok",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Auth API")
                                .summary("access token 검증 V1")
                                .description("`accessToken`의 유효성을 검증합니다.")
                                .build())
                ));
    }

    @Test
    @DisplayName("access token 검증 V1 - 401 Unauthorized")
    void checkV1_Unauthorized() throws Exception {
        // given
        given(jwtTokenProvider.getTokenStatus("invalid-token")).willReturn(TokenStatus.INVALID);

        // when & then
        mockMvc.perform(get("/api/v1/auth/check")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andDo(document("v1-get-auth-check-unauthorized",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Auth API")
                                .summary("access token 검증 V1")
                                .responseFields(ApiDocSnippets.getProblemDetailResponseFieldsWithAuthProperties())
                                .build())
                ));
    }

    @Test
    @DisplayName("회원 탈퇴 V1 - 204 No Content")
    void withdrawV1_NoContent() throws Exception {
        // given
        given(jwtTokenProvider.getTokenStatus("valid-token")).willReturn(TokenStatus.VALID);
        willDoNothing().given(authService).withdraw(any());

        // when & then
        mockMvc.perform(delete("/api/v1/auth/withdraw")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isNoContent())
                .andDo(document("v1-delete-auth-withdraw-no-content",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Auth API")
                                .summary("회원 탈퇴 V1")
                                .description("서버에서 회원을 탈퇴 처리합니다.")
                                .build())
                ));
    }
}
