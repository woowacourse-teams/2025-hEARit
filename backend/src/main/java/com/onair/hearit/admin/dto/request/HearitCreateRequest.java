package com.onair.hearit.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public record HearitCreateRequest(
        @NotBlank String title,
        @NotBlank String summary,
        @NotNull Integer playTime,
        MultipartFile originalAudio,
        MultipartFile shortAudio,
        MultipartFile scriptFile,
        @NotBlank String source,
        @NotNull Long categoryId,
        List<Long> keywordIds
) {
}
