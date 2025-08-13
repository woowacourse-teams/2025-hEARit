package com.onair.hearit.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record OAuthLoginRequest(
        @NotBlank
        String accessToken
) {
}
