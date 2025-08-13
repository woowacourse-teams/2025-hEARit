package com.onair.hearit.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.onair.hearit.auth.dto.request.OAuthLoginRequest;
import com.onair.hearit.auth.dto.request.OAuthUserInfo;
import com.onair.hearit.auth.dto.response.LoginTokenResponse;
import com.onair.hearit.auth.infrastructure.jwt.JwtTokenProvider;
import com.onair.hearit.config.TestJpaAuditingConfig;
import com.onair.hearit.domain.Member;
import com.onair.hearit.fixture.DbHelper;
import com.onair.hearit.infrastructure.MemberRepository;
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
        assertThat(memberRepository.findBySocialId(socialId)).isEmpty(); // 회원 정보가 없음을 확인

        String accessToken = "test-access-token";
        OAuthProvider provider = OAuthProvider.KAKAO;
        OAuthLoginRequest request = new OAuthLoginRequest(accessToken);
        OAuthUserInfo userInfo = new OAuthUserInfo(socialId, "테스트유저", "profile.jpg", provider);

        when(oAuthServiceRegistry.get(provider)).thenReturn(oAuthService);
        when(oAuthService.fetchUser(accessToken)).thenReturn(userInfo);

        // when
        LoginTokenResponse response = authService.loginOrSignUp(request, OAuthProvider.KAKAO);

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(response.accessToken()).isNotNull();
            softly.assertThat(response.refreshToken()).isNotNull();

            Member member = memberRepository.findBySocialId(userInfo.id()).orElseThrow();
            softly.assertThat(member.getNickname()).isEqualTo(userInfo.nickname());
        });
    }

    @Test
    @DisplayName("이미 존재하는 회원은 로그인 시 엑세스토큰 + 리프레시토큰이 발급된다")
    void loginIfAlreadyExists_thenReturnJwtOnly() {
        // given
        String kakaoId = "12345678";
        String nickname = "존재하는유저";
        String profileImage = "프로필이미지.URL";
        Member saved = memberRepository.save(Member.createSocialUser(kakaoId, nickname, profileImage));
        assertThat(memberRepository.findBySocialId(kakaoId)).isPresent(); // 회원 정보가 이미 있음을 확인

        String accessToken = "test-access-token";
        OAuthProvider provider = OAuthProvider.KAKAO;
        OAuthLoginRequest request = new OAuthLoginRequest(accessToken);
        OAuthUserInfo userInfo = new OAuthUserInfo(saved.getSocialId(), "테스트유저", "profile.jpg", provider);

        when(oAuthServiceRegistry.get(provider)).thenReturn(oAuthService);
        when(oAuthService.fetchUser(accessToken)).thenReturn(userInfo);

        // when
        LoginTokenResponse response = authService.loginOrSignUp(request, OAuthProvider.KAKAO);

        // then
        assertThat(response.accessToken()).isNotBlank();
        Member member = memberRepository.findBySocialId(kakaoId).orElseThrow();
        assertThat(member.getId()).isEqualTo(saved.getId());
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
