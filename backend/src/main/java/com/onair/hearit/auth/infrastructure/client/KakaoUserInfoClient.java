package com.onair.hearit.auth.infrastructure.client;

import com.onair.hearit.auth.dto.response.KakaoUserInfoResponse;
import com.onair.hearit.auth.exception.KakaoErrorHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class KakaoUserInfoClient {

    private final RestClient kakaoRestClient;
    private final KakaoErrorHandler kakaoErrorHandler;

    public KakaoUserInfoResponse getUserInfo(String kakaoAccessToken) {
        try {
            return kakaoRestClient.get()
                    .uri("/v2/user/me")
                    .header("Authorization", "Bearer " + kakaoAccessToken)
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            kakaoErrorHandler
                    )
                    .body(KakaoUserInfoResponse.class);
        } catch (Exception e) {
            //TODO: 적절한 예외 (커스텀 예외? + 예외처리 더 생각
            throw new IllegalArgumentException("유효하지 않은 카카오 액세스 토큰입니다.", e);
        }
    }
}
