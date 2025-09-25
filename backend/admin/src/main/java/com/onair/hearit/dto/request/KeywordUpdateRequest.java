package com.onair.hearit.dto.request;

import jakarta.validation.constraints.NotBlank;

public record KeywordUpdateRequest(
        @NotBlank String name
) {
}
