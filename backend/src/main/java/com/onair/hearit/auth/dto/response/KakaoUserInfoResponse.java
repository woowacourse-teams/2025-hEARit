package com.onair.hearit.auth.dto.response;

public record KakaoUserInfoResponse(
        String id,
        Properties properties
) {
    public record Properties(
            String nickname
    ) {
    }

    public String nickname() {
        return this.properties().nickname();
    }
}
