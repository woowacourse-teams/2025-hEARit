package com.onair.hearit.admin.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record HearitMetaDataRequest(
        @NotBlank String title,
        @NotBlank String summary,
        @NotNull Integer playTime,
        @NotNull Long categoryId,
        List<Long> keywordIds,
        @NotEmpty List<@Valid SourceCreateRequest> sources,
        @NotNull String originalAudioKey,
        @NotNull String shortAudioKey,
        @NotNull String scriptFileKey
) {

    public record SourceCreateRequest(
            @NotBlank String sourceName,
            String sourceUrl
    ) {
    }
}
