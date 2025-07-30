package com.onair.hearit.auth.dto.response;

public record LoginTokenResponse(
        String accessToken,
        String refreshToken
) {
}
