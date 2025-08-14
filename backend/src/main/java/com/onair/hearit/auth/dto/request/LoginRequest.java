package com.onair.hearit.auth.dto.request;

public record LoginRequest(
        String localId,
        String password
) {
}
