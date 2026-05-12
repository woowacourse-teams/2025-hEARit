package com.onair.hearit.admin.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SeriesCreateRequest(
        @NotBlank String title,
        String description,
        String imageKey
) {
}
