package com.onair.hearit.admin.ai.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MetadataUpdateRequest(
        @NotBlank @Size(max = 35) String title,
        @NotBlank @Size(max = 250) String summary
) {
}
