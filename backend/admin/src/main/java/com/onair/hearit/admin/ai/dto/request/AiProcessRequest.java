package com.onair.hearit.admin.ai.dto.request;

import org.springframework.web.multipart.MultipartFile;

public record AiProcessRequest(
        MultipartFile audioFile
) {
}
