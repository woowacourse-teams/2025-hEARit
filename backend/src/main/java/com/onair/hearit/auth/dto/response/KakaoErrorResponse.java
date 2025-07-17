package com.onair.hearit.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KakaoErrorResponse(
        String msg,
        String code
) {
}
