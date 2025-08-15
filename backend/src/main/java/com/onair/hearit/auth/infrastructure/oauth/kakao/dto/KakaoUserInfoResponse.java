package com.onair.hearit.auth.infrastructure.oauth.kakao.dto;

public record KakaoUserInfoResponse(
        String id,
        Properties properties
) {
    public String nickname() {
        return this.properties().nickname();
    }

    public String profileImage() {
        return this.properties().profile_image();
    }

    public record Properties(
            String nickname,
            String profile_image
    ) {
    }
}
