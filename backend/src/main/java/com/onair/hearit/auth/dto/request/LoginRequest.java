package com.onair.hearit.auth.dto.request;

public record LoginRequest(
        String memberId,
        String password
) {
}
