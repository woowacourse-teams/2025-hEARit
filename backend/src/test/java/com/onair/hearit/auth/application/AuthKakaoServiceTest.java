package com.onair.hearit.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.when;

import com.onair.hearit.auth.domain.OAuthProvider;
import com.onair.hearit.auth.dto.request.OAuthLoginRequest;
import com.onair.hearit.auth.dto.response.LoginTokenResponse;
import com.onair.hearit.auth.dto.response.OAuthUserInfoResponse;
import com.onair.hearit.auth.infrastructure.jwt.JwtTokenProvider;
import com.onair.hearit.common.infrastructure.jpa.TestJpaAuditingConfig;
import com.onair.hearit.common.domain.Member;
import com.onair.hearit.fixture.DbHelper;
import com.onair.hearit.common.infrastructure.jpa.MemberRepository;
import java.util.UUID;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DataJpaTest
@ActiveProfiles("fake-test")
@Import({AuthService.class, BCryptPasswordEncoder.class, JwtTokenProvider.class,
        DbHelper.class, TestJpaAuditingConfig.class})
class AuthKakaoServiceTest {

    @MockitoBean
    OAuthServiceRegistry oAuthServiceRegistry;

    @MockitoBean
    OAuthService oAuthService;

    @Autowired
    AuthService authService;

    @Autowired
    MemberRepository memberRepository;

    @Test
    @DisplayName("처음 카카오 로그인 시, 자동 회원가입 후 JWT를 발급한다")
    void signupIfNotExists_thenReturnJwt() {
        // given
        String socialId = "kakao-12345";
        OAuthProvider provider = OAuthProvider.KAKAO;
        assertThat(memberRepository.findBySocialIdAndOAuthProvider(socialId, provider)).isEmpty(); // 회원 정보가 없음을 확인

        String accessToken = "test-access-token";
        OAuthLoginRequest request = new OAuthLoginRequest(accessToken);
        OAuthUserInfoResponse userInfo = new OAuthUserInfoResponse(socialId, "테스트유저", "profile.jpg", provider);

        when(oAuthServiceRegistry.get(provider)).thenReturn(oAuthService);
        when(oAuthService.fetchUser(accessToken)).thenReturn(userInfo);

        // when
        LoginTokenResponse response = authService.loginOrSignUp(request, OAuthProvider.KAKAO);

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(response.accessToken()).isNotNull();
            softly.assertThat(response.refreshToken()).isNotNull();

            Member member = memberRepository.findBySocialIdAndOAuthProvider(userInfo.id(), provider).orElseThrow();
            softly.assertThat(member.getNickname()).isEqualTo(userInfo.nickname());
            assertThat(member.getOAuthProvider()).isEqualTo(OAuthProvider.KAKAO);
        });
    }

    @Test
    @DisplayName("이미 존재하는 회원은 로그인 시 엑세스토큰 + 리프레시토큰이 발급된다")
    void loginIfAlreadyExists_thenReturnJwtOnly() {
        // given
        String kakaoId = "12345678";
        String nickname = "존재하는유저";
        String profileImage = "프로필이미지.URL";
        OAuthProvider provider = OAuthProvider.KAKAO;
        Member saved = memberRepository.save(
                Member.createSocialUser(UUID.randomUUID(), kakaoId, nickname, profileImage, provider));
        assertThat(memberRepository.findBySocialIdAndOAuthProvider(kakaoId, provider)).isPresent(); // 회원 정보가 이미 있음을 확인

        String accessToken = "test-access-token";
        OAuthLoginRequest request = new OAuthLoginRequest(accessToken);
        OAuthUserInfoResponse userInfo = new OAuthUserInfoResponse(saved.getSocialId(), "테스트유저", "profile.jpg",
                provider);

        when(oAuthServiceRegistry.get(provider)).thenReturn(oAuthService);
        when(oAuthService.fetchUser(accessToken)).thenReturn(userInfo);

        // when
        LoginTokenResponse response = authService.loginOrSignUp(request, OAuthProvider.KAKAO);

        // then
        assertAll(() -> {
            assertThat(response.accessToken()).isNotBlank();
            Member member = memberRepository.findBySocialIdAndOAuthProvider(kakaoId, provider).orElseThrow();
            assertThat(member.getId()).isEqualTo(saved.getId());
            assertThat(member.getOAuthProvider()).isEqualTo(OAuthProvider.KAKAO);
        });
    }

    @Test
    @DisplayName("카카오 엑세스토이 유효하지 않으면 예외를 반환한다")
    void invalidKakaoAccessToken_thenThrowException() {
        // given
        String invalidToken = "invalid-token";
        OAuthLoginRequest request = new OAuthLoginRequest(invalidToken);
        OAuthProvider provider = OAuthProvider.KAKAO;

        when(oAuthServiceRegistry.get(provider)).thenReturn(oAuthService);
        when(oAuthService.fetchUser(invalidToken)).thenThrow(new IllegalArgumentException("유효하지 않은 카카오 액세스 토큰입니다."));

        // when & then
        assertThatThrownBy(() -> authService.loginOrSignUp(request, OAuthProvider.KAKAO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유효하지 않은 카카오 액세스 토큰");
    }
}
