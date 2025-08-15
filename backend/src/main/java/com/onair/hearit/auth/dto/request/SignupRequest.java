package com.onair.hearit.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SignupRequest(
        @NotBlank
        String localId,

        @NotBlank
        String nickname,

        @NotBlank
        String password
) {
}
