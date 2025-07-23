package com.onair.hearit.auth.dto.response;

public record KakaoUserInfoResponse(
        String id,
        Properties properties
) {
    public record Properties(
            String nickname,
            String profile_image
    ) {
    }

    public String nickname() {
        return this.properties().nickname();
    }

    public String profileImage() {
        return this.properties().profile_image();
    }
}
