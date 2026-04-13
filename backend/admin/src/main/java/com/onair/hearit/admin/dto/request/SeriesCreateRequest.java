package com.onair.hearit.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SeriesCreateRequest(
        @NotBlank @Size(max = 50) String title,
        @Size(max = 500) String description,
        String imageKey
) {
}
