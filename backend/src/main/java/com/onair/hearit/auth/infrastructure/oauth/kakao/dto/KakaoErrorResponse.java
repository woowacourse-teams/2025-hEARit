package com.onair.hearit.auth.infrastructure.oauth.kakao.dto;

public record KakaoErrorResponse(
        String msg,
        String code
) {
}
