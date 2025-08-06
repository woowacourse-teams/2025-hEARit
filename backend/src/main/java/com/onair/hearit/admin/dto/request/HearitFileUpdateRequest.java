package com.onair.hearit.admin.dto.request;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record HearitFileUpdateRequest(
        @NotNull MultipartFile file
) {
}
