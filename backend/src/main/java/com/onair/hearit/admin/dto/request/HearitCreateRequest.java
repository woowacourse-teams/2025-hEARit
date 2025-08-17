package com.onair.hearit.admin.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public record HearitCreateRequest(
        @NotBlank String title,
        @NotBlank String summary,
        @NotNull Integer playTime,
        @NotNull MultipartFile originalAudio,
        @NotNull MultipartFile shortAudio,
        @NotNull MultipartFile scriptFile,
        @NotEmpty List<@Valid SourceCreateRequest> sources,
        @NotNull Long categoryId,
        List<Long> keywordIds
) {

    public record SourceCreateRequest(
            @NotBlank String sourceName,
            String sourceUrl
    ) {
    }
}
