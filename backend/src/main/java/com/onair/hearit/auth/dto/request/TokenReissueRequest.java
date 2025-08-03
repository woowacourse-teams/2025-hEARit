package com.onair.hearit.auth.dto.request;

public record TokenReissueRequest(
        String refreshToken
) {
}
