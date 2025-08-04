package com.onair.hearit.auth.presentation;

import com.onair.hearit.auth.application.AuthService;
import com.onair.hearit.auth.dto.CurrentMember;
import com.onair.hearit.auth.dto.request.KakaoLoginRequest;
import com.onair.hearit.auth.dto.request.LoginRequest;
import com.onair.hearit.auth.dto.request.SignupRequest;
import com.onair.hearit.auth.dto.request.TokenReissueRequest;
import com.onair.hearit.auth.dto.response.LoginTokenResponse;
import com.onair.hearit.auth.dto.response.TokenReissueResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginTokenResponse> login(@RequestBody LoginRequest request) {
        LoginTokenResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/kakao-login")
    public ResponseEntity<LoginTokenResponse> loginOrSignupWithKakao(@RequestBody KakaoLoginRequest request) {
        LoginTokenResponse response = authService.loginOrSignupWithKakao(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@RequestBody SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
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
