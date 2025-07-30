package com.onair.hearit.auth.presentation;

import com.onair.hearit.auth.application.AuthService;
import com.onair.hearit.auth.dto.CurrentMember;
import com.onair.hearit.auth.dto.request.KakaoLoginRequest;
import com.onair.hearit.auth.dto.request.LoginRequest;
import com.onair.hearit.auth.dto.request.SignupRequest;
import com.onair.hearit.auth.dto.request.TokenReissueRequest;
import com.onair.hearit.auth.dto.response.LoginTokenResponse;
import com.onair.hearit.auth.dto.response.TokenReissueResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "사용자 인증")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "일반 로그인", description = "아이디/비밀번호로 로그인하여 토큰을 발급받습니다.")
    @PostMapping("/login")
    public ResponseEntity<LoginTokenResponse> login(@RequestBody LoginRequest request) {
        LoginTokenResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "카카오 로그인", description = "카카오 액세스토큰으로 로그인 시 토큰을 발급받습니다.")
    @PostMapping("/kakao-login")
    public ResponseEntity<LoginTokenResponse> loginWithKakao(@RequestBody KakaoLoginRequest request) {
        LoginTokenResponse response = authService.loginWithKakao(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "회원가입", description = "새로운 계정을 생성합니다.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "회원가입 성공"),
                    @ApiResponse(responseCode = "400", description = "중복된 이메일",
                            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
            })
    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@RequestBody SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.created(URI.create("/")).build();
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<TokenReissueResponse> reissue(@RequestBody TokenReissueRequest request) {
        String newAccessToken = authService.reissue(request.refreshToken());
        return ResponseEntity.ok(new TokenReissueResponse(newAccessToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal CurrentMember currentMember) {
        authService.logout(currentMember.memberId());
        return ResponseEntity.noContent().build();
    }
}
