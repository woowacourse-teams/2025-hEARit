package com.onair.hearit.app.auth.presentation;

import com.onair.hearit.app.auth.application.AuthService;
import com.onair.hearit.app.auth.domain.RequestUser;
import com.onair.hearit.app.auth.dto.request.LoginRequest;
import com.onair.hearit.app.auth.dto.request.OAuthLoginRequest;
import com.onair.hearit.app.auth.dto.request.SignupRequest;
import com.onair.hearit.app.auth.dto.request.TokenReissueRequest;
import com.onair.hearit.app.auth.dto.response.LoginTokenResponse;
import com.onair.hearit.app.auth.dto.response.TokenReissueResponse;
import com.onair.hearit.core.domain.OAuthProvider;
import com.onair.hearit.core.domain.UserInfo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
    public ResponseEntity<LoginTokenResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginTokenResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/kakao-login")
    public ResponseEntity<LoginTokenResponse> loginOrSignupWithKakao(@Valid @RequestBody OAuthLoginRequest request) {
        LoginTokenResponse response = authService.loginOrSignUp(request, OAuthProvider.KAKAO);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@Valid @RequestBody SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<TokenReissueResponse> reissue(@Valid @RequestBody TokenReissueRequest request) {
        String newAccessToken = authService.reissue(request.refreshToken());
        return ResponseEntity.ok(new TokenReissueResponse(newAccessToken));
    }

    @GetMapping("/check")
    public ResponseEntity<Void> checkAccessToken() {
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/withdraw")
    public ResponseEntity<Void> withdraw(@AuthenticationPrincipal RequestUser requestUser) {
        UserInfo userInfo = requestUser.getUserInfo();
        authService.withdraw(userInfo.getMemberUuid());
        return ResponseEntity.noContent().build();
    }
}
