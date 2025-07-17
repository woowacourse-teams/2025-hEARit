package com.onair.hearit.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.onair.hearit.DbHelper;
import com.onair.hearit.auth.Infrastructure.client.KakaoUserInfoClient;
import com.onair.hearit.auth.Infrastructure.jwt.JwtTokenProvider;
import com.onair.hearit.auth.dto.request.KakaoLoginRequest;
import com.onair.hearit.auth.dto.response.KakaoUserInfoResponse;
import com.onair.hearit.auth.dto.response.TokenResponse;
import com.onair.hearit.domain.Member;
import com.onair.hearit.infrastructure.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@DataJpaTest
@Import({AuthService.class, BCryptPasswordEncoder.class, JwtTokenProvider.class, DbHelper.class})
@ActiveProfiles("fake-test")
class AuthKakaoServiceTest {

    @MockitoBean
    KakaoUserInfoClient kakaoUserInfoClient;

    @Autowired
    AuthService authService;

    @Autowired
    MemberRepository memberRepository;

    @Test
    @DisplayName("처음 카카오 로그인 시, 자동 회원가입 후 JWT를 발급한다")
    void signupIfNotExists_thenReturnJwt() {
        // given
        assertThat(memberRepository.findBySocialId("12345678")).isEmpty(); // 회원 정보가 없음을 확인

        String kakaoId = "12345678";
        String nickname = "새로운유저";
        KakaoUserInfoResponse kakaoUserInfo = new KakaoUserInfoResponse(
                kakaoId
                , new KakaoUserInfoResponse.Properties(nickname));
        String accessToken = "valid-token";
        Mockito.when(kakaoUserInfoClient.getUserInfo(accessToken)).thenReturn(kakaoUserInfo);

        KakaoLoginRequest request = new KakaoLoginRequest(accessToken);

        // when
        TokenResponse response = authService.loginWithKakao(request);

        // then
        assertThat(response.accessToken()).isNotBlank();
        Member member = memberRepository.findBySocialId(kakaoId).orElseThrow();
        assertThat(member.getNickname()).isEqualTo(nickname);
    }

    @Test
    @DisplayName("이미 존재하는 회원은 JWT만 발급된다")
    void loginIfAlreadyExists_thenReturnJwtOnly() {
        // given
        String kakaoId = "12345678";
        String nickname = "존재하는유저";
        Member saved = memberRepository.save(Member.createSocialUser(kakaoId, nickname));
        assertThat(memberRepository.findBySocialId(kakaoId)).isPresent(); // 회원 정보가 이미 있음을 확인

        String accessToken = "valid-token";
        KakaoUserInfoResponse kakaoUserInfo = new KakaoUserInfoResponse(
                kakaoId, new KakaoUserInfoResponse.Properties(nickname));
        Mockito.when(kakaoUserInfoClient.getUserInfo(accessToken)).thenReturn(kakaoUserInfo);

        KakaoLoginRequest request = new KakaoLoginRequest(accessToken);

        // when
        TokenResponse response = authService.loginWithKakao(request);

        // then
        assertThat(response.accessToken()).isNotBlank();
        Member member = memberRepository.findBySocialId(kakaoId).orElseThrow();
        assertThat(member.getId()).isEqualTo(saved.getId());
    }

    @Test
    @DisplayName("카카오 AccessToken이 유효하지 않으면 예외를 반환한다")
    void invalidKakaoAccessToken_thenThrowException() {
        // given
        String invalidToken = "invalid-token";
        Mockito.when(kakaoUserInfoClient.getUserInfo(invalidToken))
                .thenThrow(new IllegalArgumentException("유효하지 않은 카카오 액세스 토큰입니다."));
        KakaoLoginRequest request = new KakaoLoginRequest(invalidToken);

        // when & then
        assertThatThrownBy(() -> authService.loginWithKakao(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유효하지 않은 카카오 액세스 토큰");
    }
}
