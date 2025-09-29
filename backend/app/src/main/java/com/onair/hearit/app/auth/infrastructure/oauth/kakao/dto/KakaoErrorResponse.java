package com.onair.hearit.app.auth.infrastructure.oauth.kakao.dto;

public record KakaoErrorResponse(
        String msg,
        String code
) {
}
