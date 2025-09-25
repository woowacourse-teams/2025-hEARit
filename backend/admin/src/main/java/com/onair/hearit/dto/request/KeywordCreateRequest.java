package com.onair.hearit.dto.request;

import jakarta.validation.constraints.NotBlank;

public record KeywordCreateRequest(
        @NotBlank String name
) {
}
