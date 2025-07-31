package com.onair.hearit.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.auth.dto.request.LoginRequest;
import com.onair.hearit.auth.dto.request.SignupRequest;
import com.onair.hearit.auth.dto.response.LoginTokenResponse;
import com.onair.hearit.auth.infrastructure.client.KakaoUserInfoClient;
import com.onair.hearit.auth.infrastructure.jwt.JwtTokenProvider;
import com.onair.hearit.auth.infrastructure.jwt.RefreshToken;
import com.onair.hearit.auth.infrastructure.jwt.RefreshTokenRepository;
import com.onair.hearit.common.exception.custom.InvalidInputException;
import com.onair.hearit.common.exception.custom.UnauthorizedException;
import com.onair.hearit.config.TestJpaAuditingConfig;
import com.onair.hearit.domain.Member;
import com.onair.hearit.fixture.DbHelper;
import com.onair.hearit.fixture.TestFixture;
import com.onair.hearit.infrastructure.MemberRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
@Import({AuthService.class, BCryptPasswordEncoder.class, JwtTokenProvider.class,
        DbHelper.class, TestJpaAuditingConfig.class})
class AuthServiceTest {

    @MockitoBean
    KakaoUserInfoClient kakaoUserInfoClient;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    @Autowired
    DbHelper dbHelper;

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("아이디와 비밀번호로 회원가입할 수 있으며, 기본 프로필 이미지로 저장된다.")
    void signup_success() {
        // given
        SignupRequest request = new SignupRequest("localId", "nickname", "password123");

        // when
        authService.signup(request);

        // then
        Member saved = memberRepository.findByLocalId("localId").orElseThrow();
        assertAll(() -> {
            assertThat(saved.getNickname()).isEqualTo("nickname");
            assertThat(passwordEncoder.matches("password123", saved.getPassword())).isTrue();
            assertThat(saved.getProfileImage()).isNotNull();
        });
    }

    @Test
    @DisplayName("이미 존재하는 아이디로 회원가입 시 예외가 발생한다.")
    void signup_duplicate_id() {
        // given
        dbHelper.insertMember(
                Member.createLocalUser("sameId", "nickname", passwordEncoder.encode("password"), "profile.jpg"));

        SignupRequest signupRequest = new SignupRequest("sameId", "another", "password");

        // when & then
        assertThatThrownBy(() -> authService.signup(signupRequest))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("이미 존재하는 아이디입니다.");
    }

    @Test
    @DisplayName("로그인 성공 시 엑세스토큰 + 리프래시토큰을 발급한다.")
    void login_success() {
        // given
        dbHelper.insertMember(
                Member.createLocalUser("localId", "nickname", passwordEncoder.encode("password"), "profile.jpg"));

        LoginRequest loginRequest = new LoginRequest("localId", "password");

        // when
        LoginTokenResponse loginTokenResponse = authService.login(loginRequest);

        // then
        String responseAccessToken = loginTokenResponse.accessToken();
        String responseRefreshToken = loginTokenResponse.refreshToken();
        assertAll(() -> {
            assertThat(responseAccessToken).isNotNull();
            assertThat(responseRefreshToken).isNotNull();
            RefreshToken refreshToken = refreshTokenRepository.findByToken(responseRefreshToken).orElseThrow();
            assertThat(refreshToken.getToken()).isEqualTo(responseRefreshToken);
        });
    }

    @Nested
    @DisplayName("토큰 재발급")
    class Reissue {

        @Test
        @DisplayName("리프레시 토큰이 유효하고 저장된 값과 일치하면 accessToken을 재발급한다")
        void reissue_success() {
            // given
            Member member = dbHelper.insertMember(
                    Member.createLocalUser("localId", "nickname", passwordEncoder.encode("password"), "profile.jpg"));
            String refreshTokenValue = jwtTokenProvider.createRefreshToken(member.getId());
            refreshTokenRepository.save(new RefreshToken(member.getId(), refreshTokenValue, LocalDateTime.now()));

            // when
            String reissuedAccessToken = authService.reissue(refreshTokenValue);

            // then
            assertThat(reissuedAccessToken).isNotNull();
        }

        @Test
        @DisplayName("리프레시 토큰이 만료되었으면 UnauthorizedException을 던진다")
        void reissue_fail_when_refreshToken_expired() throws InterruptedException {
            // given
            Member member = dbHelper.insertMember(
                    Member.createLocalUser("localId", "nickname", passwordEncoder.encode("password"), "profile.jpg"));
            String refreshTokenValue = jwtTokenProvider.createRefreshToken(member.getId());
            refreshTokenRepository.save(new RefreshToken(member.getId(), refreshTokenValue, LocalDateTime.now()));

            Thread.sleep(1000); // 리프레시 토큰 유효시간 1초 -> 1초 기다려서 토큰 만료시킴

            // when & then
            assertThatThrownBy(() -> authService.reissue(refreshTokenValue))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("만료된 리프레시토큰입니다.");
        }

        @Test
        @DisplayName("저장된 리프레시 토큰이 없으면 UnauthorizedException을 던진다")
        void reissue_fail_when_refreshToken_not_found_in_db() {
            // given
            Member member = dbHelper.insertMember(
                    Member.createLocalUser("localId", "nickname", passwordEncoder.encode("password"), "profile.jpg"));
            String refreshTokenValue = jwtTokenProvider.createRefreshToken(member.getId());
            // 리프레시토큰 DB에 저장 안 함

            // when & then
            assertThatThrownBy(() -> authService.reissue(refreshTokenValue))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("저장된 토큰이 없습니다.");
        }

        @Test
        @DisplayName("리프레시 토큰 값이 DB에 저장된 것과 다르면 UnauthorizedException을 던진다")
        void reissue_fail_when_refreshToken_mismatch() {
            // given
            Member member = dbHelper.insertMember(
                    Member.createLocalUser("localId", "nickname", passwordEncoder.encode("password"), "profile.jpg"));
            String refreshTokenValue = jwtTokenProvider.createRefreshToken(member.getId());
            // 다른 리프레시토큰 저장
            refreshTokenRepository.save(new RefreshToken(member.getId(), "other-refresh-token", LocalDateTime.now()));

            // when & then
            assertThatThrownBy(() -> authService.reissue(refreshTokenValue))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("리프레시 토큰이 불일치합니다.");
        }
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
        dbHelper.insertMember(Member.createLocalUser("localId", "nickname", "password", "profile.jpg"));

        LoginRequest loginRequest = new LoginRequest("localId", "wrongpassword");

        // when & then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("아이디나 비밀번호가 일치하지 않습니다.");
    }

    @Test
    @DisplayName("로그아웃 시 해당 member의 refreshToken을 삭제한다.")
    void logout_then_deleteRefreshToken() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getId());
        refreshTokenRepository.save(new RefreshToken(member.getId(), refreshToken, LocalDateTime.now()));
        assertThat(refreshTokenRepository.findByMemberId(member.getId())).isPresent();

        // when
        authService.logout(member.getId());

        // then
        assertThat(refreshTokenRepository.findByMemberId(member.getId())).isEmpty();
    }
}
