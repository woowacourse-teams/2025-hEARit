package com.onair.hearit.admin.dto.request;

import java.util.List;

public record HearitCreateRequest(
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
