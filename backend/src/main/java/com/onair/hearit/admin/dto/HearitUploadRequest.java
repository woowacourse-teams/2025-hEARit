package com.onair.hearit.admin.dto;

import java.util.List;

public record HearitUploadRequest(
        String title,
        String summary,
        Integer playTime,
        String originalAudioUrl,
        String shortAudioUrl,
        String scriptUrl,
        String source,
        Long categoryId,
        List<Long> keywordIds
) {
}
