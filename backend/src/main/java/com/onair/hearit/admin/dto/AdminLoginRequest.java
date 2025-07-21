package com.onair.hearit.admin.dto;

public record AdminLoginRequest(
        String loginId,
        String password
) {
}
