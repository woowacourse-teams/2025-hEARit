package com.onair.hearit.auth.dto.response;

public record KakaoErrorResponse(
        String msg,
        String code
) {
}
