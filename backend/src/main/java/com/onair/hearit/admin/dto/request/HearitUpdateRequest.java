package com.onair.hearit.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record HearitUpdateRequest(
        @NotBlank String title,
        @NotBlank String summary,
        @NotNull Integer playTime,
        @NotBlank String originalAudioUrl,
        @NotBlank String shortAudioUrl,
        @NotBlank String scriptUrl,
        @NotBlank String source,
        @NotNull Long categoryId,
        List<Long> keywordIds
) {
}
