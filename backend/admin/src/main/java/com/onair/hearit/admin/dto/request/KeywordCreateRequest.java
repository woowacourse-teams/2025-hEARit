package com.onair.hearit.admin.dto.request;

import jakarta.validation.constraints.NotBlank;

public record KeywordCreateRequest(
        @NotBlank String name
) {
}
