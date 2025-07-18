package com.onair.hearit.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.onair.hearit.DbHelper;
import com.onair.hearit.auth.Infrastructure.client.KakaoUserInfoClient;
import com.onair.hearit.auth.Infrastructure.jwt.JwtTokenProvider;
import com.onair.hearit.auth.dto.request.LoginRequest;
import com.onair.hearit.auth.dto.request.SignupRequest;
import com.onair.hearit.auth.dto.response.TokenResponse;
import com.onair.hearit.common.exception.custom.InvalidInputException;
import com.onair.hearit.common.exception.custom.UnauthorizedException;
import com.onair.hearit.domain.Member;
import com.onair.hearit.infrastructure.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DataJpaTest
@ActiveProfiles("fake-test")
@Import({AuthService.class, BCryptPasswordEncoder.class, JwtTokenProvider.class, DbHelper.class})
class AuthServiceTest {

    @MockitoBean
    KakaoUserInfoClient kakaoUserInfoClient;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    DbHelper dbHelper;
    @Autowired
    private AuthService authService;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("아이디와 비밀번호로 회원가입할 수 있다")
    void signup_success() {
        // given
        SignupRequest request = new SignupRequest("localId", "nickname", "password123");

        // when
        authService.signup(request);

        // then
        Member saved = memberRepository.findByLocalId("localId").orElseThrow();
        assertThat(saved.getNickname()).isEqualTo("nickname");
        assertThat(passwordEncoder.matches("password123", saved.getPassword())).isTrue();
    }

    @Test
    @DisplayName("이미 존재하는 아이디로 회원가입 시 예외가 발생한다.")
    void signup_duplicate_id() {
        // given
        dbHelper.insertMember(
                Member.createLocalUser("sameId", "nickname", "password"));

        SignupRequest signupRequest = new SignupRequest("sameId", "another", "password");

        // when & then
        assertThatThrownBy(() -> authService.signup(signupRequest))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("이미 존재하는 아이디입니다.");
    }

    @Test
    @DisplayName("로그인 성공 시 토큰을 반환한다.")
    void login_success() {
        // given
        dbHelper.insertMember(Member.createLocalUser("localId", "nickname", passwordEncoder.encode("password")));

        LoginRequest loginRequest = new LoginRequest("localId", "password");

        // when
        TokenResponse tokenResponse = authService.login(loginRequest);

        // then
        assertThat(tokenResponse.accessToken()).isNotNull();
    }

    @Test
    @DisplayName("아이디가 존재하지 않을 경우 인증예외가 발생한다")
    void login_fail_member_not_found() {
        // given
        LoginRequest request = new LoginRequest("nonexistent", "password");

        // when & then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("아이디나 비밀번호가 일치하지 않습니다.");
    }

    @Test
    @DisplayName("비밀번호가 틀릴 경우 인증예외가 발생한다")
    void login_fail_wrong_password() {
        // given
        dbHelper.insertMember(Member.createLocalUser("localId", "nickname", "password"));

        LoginRequest loginRequest = new LoginRequest("localId", "wrongpassword");

        // when & then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("아이디나 비밀번호가 일치하지 않습니다.");
    }
}
