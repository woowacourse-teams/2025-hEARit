package com.onair.hearit.admin.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CategoryCreateRequest(
        @NotBlank String name,
        @NotBlank String colorCode
) {
}
