package com.onair.hearit.admin.dto.request;

public record AdminLoginRequest(
        String loginId,
        String password
) {
}
