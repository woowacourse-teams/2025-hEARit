package com.onair.hearit.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TokenReissueRequest(
        @NotBlank
        String refreshToken
) {
}
