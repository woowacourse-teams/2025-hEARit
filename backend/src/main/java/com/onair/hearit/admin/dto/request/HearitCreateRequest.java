package com.onair.hearit.admin.dto.request;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public record HearitCreateRequest(
        String title,
        String summary,
        Integer playTime,
        MultipartFile originalAudio,
        MultipartFile shortAudio,
        MultipartFile scriptFile,
        String source,
        Long categoryId,
        List<Long> keywordIds
) {
}
