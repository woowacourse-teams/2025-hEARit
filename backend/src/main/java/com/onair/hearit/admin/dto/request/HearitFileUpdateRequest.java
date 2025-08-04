package com.onair.hearit.admin.dto.request;

import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.annotations.NotNull;

public record HearitFileUpdateRequest(
        @NotNull MultipartFile file
) {
}
