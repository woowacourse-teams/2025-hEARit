package com.onair.hearit.auth.dto.request;

public record SignupRequest(
        String localId,
        String nickname,
        String password
) {
}
