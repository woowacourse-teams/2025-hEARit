package com.onair.hearit.admin.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UploadUrlRequest(
        @NotBlank String originalAudioFileName,
        @NotBlank String shortAudioFileName,
        @NotBlank String scriptFileName
) {
}
