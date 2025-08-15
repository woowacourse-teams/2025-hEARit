package com.onair.hearit.auth.infrastructure.oauth.kakao;

import com.onair.hearit.auth.application.OAuthService;
import com.onair.hearit.auth.domain.OAuthProvider;
import com.onair.hearit.auth.dto.request.OAuthUserInfo;
import com.onair.hearit.auth.dto.response.KakaoUserInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class KakaoOAuthService implements OAuthService {

    private final RestClient kakaoRestClient;

    @Override
    public OAuthProvider provider() {
        return OAuthProvider.KAKAO;
    }

    @Override
    public OAuthUserInfo fetchUser(String accessToken) {
        KakaoUserInfoResponse response = getUserInfo(accessToken);
        return new OAuthUserInfo(response.id(), response.nickname(), response.profileImage(), provider());
    }

    private KakaoUserInfoResponse getUserInfo(String kakaoAccessToken) {
        return kakaoRestClient.get()
                .uri("/v2/user/me")
                .header("Authorization", "Bearer " + kakaoAccessToken)
                .retrieve()
                .body(KakaoUserInfoResponse.class);
    }
}
