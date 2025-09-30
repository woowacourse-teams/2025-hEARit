package com.onair.hearit.app.auth.infrastructure.oauth.kakao;

import com.onair.hearit.app.auth.application.OAuthService;
import com.onair.hearit.app.auth.dto.response.OAuthUserInfoResponse;
import com.onair.hearit.app.auth.infrastructure.oauth.kakao.dto.KakaoUserInfoResponse;
import com.onair.hearit.core.domain.OAuthProvider;
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
    public OAuthUserInfoResponse fetchUser(String accessToken) {
        KakaoUserInfoResponse response = getUserInfo(accessToken);
        return new OAuthUserInfoResponse(response.id(), response.nickname(), response.profileImage(), provider());
    }

    private KakaoUserInfoResponse getUserInfo(String kakaoAccessToken) {
        return kakaoRestClient.get()
                .uri("/v2/user/me")
                .header("Authorization", "Bearer " + kakaoAccessToken)
                .retrieve()
                .body(KakaoUserInfoResponse.class);
    }
}
