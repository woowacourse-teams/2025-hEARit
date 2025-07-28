package com.onair.hearit.auth.infrastructure.client;

import com.onair.hearit.auth.dto.response.KakaoUserInfoResponse;
import com.onair.hearit.fixture.IntegrationTest;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class KakaoUserInfoClientTest extends IntegrationTest {

    @Autowired
    KakaoUserInfoClient kakaoUserInfoClient;

    @Disabled("실제 카카오 외부 API를 호출하는 테스트-필요시 활성화")
    @Test
    @DisplayName("실제 카카오 API를 호출하여 사용자정보를 가져온다.")
    void fetchUserInfo_usingRealKakaoApi() {
        String kakaoAccessToken = "yC5CmZ1kj1jHcCICRBWX_dvbXIMqfn8rAAAAAQoXBi4AAAGYE03XhpQkbXeV0h_w";

        KakaoUserInfoResponse response = kakaoUserInfoClient.getUserInfo(kakaoAccessToken);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(response).isNotNull();
            softly.assertThat(response.id()).isNotNull();
            softly.assertThat(response.nickname()).isNotNull();
        });
    }
}
