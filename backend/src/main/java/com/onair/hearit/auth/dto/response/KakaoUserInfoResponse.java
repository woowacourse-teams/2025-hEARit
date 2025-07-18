package com.onair.hearit.auth.dto.response;

public record KakaoUserInfoResponse(
        String id,
        Properties properties
) {
    public String nickname() {
        return this.properties().nickname();
    }

    public record Properties(
            String nickname
    ) {
    }
}
