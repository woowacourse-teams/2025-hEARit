package com.onair.hearit.auth.dto.request;

public record SignupRequest(
        String memberId,
        String nickname,
        String password
) {
}
