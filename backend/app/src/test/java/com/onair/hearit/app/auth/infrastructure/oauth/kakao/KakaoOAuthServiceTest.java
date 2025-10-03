package com.onair.hearit.app.auth.infrastructure.oauth.kakao;

import com.onair.hearit.app.auth.dto.response.OAuthUserInfoResponse;
import com.onair.hearit.app.fixture.IntegrationTest;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class KakaoOAuthServiceTest extends IntegrationTest {

    @Autowired
    KakaoOAuthService kakaoOAuthService;

    @Disabled("실제 카카오 외부 API를 호출하는 테스트-필요시 활성화")
    @Test
    @DisplayName("실제 카카오 API를 호출하여 사용자정보를 가져온다.")
    void fetchUserInfo_usingRealKakaoApi() {
        String kakaoAccessToken = "yC5CmZ1kj1jHcCICRBWX_dvbXIMqfn8rAAAAAQoXBi4AAAGYE03XhpQkbXeV0h_w";
        OAuthUserInfoResponse response = kakaoOAuthService.fetchUser(kakaoAccessToken);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(response).isNotNull();
            softly.assertThat(response.id()).isNotNull();
            softly.assertThat(response.nickname()).isNotNull();
        });
    }
}
