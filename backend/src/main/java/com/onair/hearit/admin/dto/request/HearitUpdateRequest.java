package com.onair.hearit.admin.dto.request;

import com.onair.hearit.domain.Source;
import jakarta.validation.Valid;
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
        @Valid List<SourceUpdateRequest> sources,
        @NotNull Long categoryId,
        List<Long> keywordIds
) {

    public record SourceUpdateRequest(
            @NotBlank String sourceName,
            @NotBlank String sourceUrl
    ) {
        public Source toSource() {
            return new Source(sourceName, sourceUrl);
        }
    }
}
