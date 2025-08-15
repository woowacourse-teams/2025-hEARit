package com.onair.hearit.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank
        String localId,

        @NotBlank
        String password
) {
}
