package com.onair.hearit.admin.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PresignedUrlRequest(
        @NotBlank String originalAudioFileName,
        @NotBlank String shortAudioFileName,
        @NotBlank String scriptFileName
) {
}
