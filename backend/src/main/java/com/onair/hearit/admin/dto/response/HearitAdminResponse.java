package com.onair.hearit.admin.dto.response;

import com.onair.hearit.domain.Hearit;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record HearitAdminResponse(
        Long id,
        String title,
        String summary,
        String originalAudioUrl,
        String shortAudioUrl,
        String scriptUrl,
        String source,
        Integer playTime,
        LocalDateTime createdAt,
        CategoryInfoResponse category,
        List<KeywordInHearit> keywords
) {
    public static HearitAdminResponse from(Hearit hearit, Map<Long, List<KeywordInHearit>> keywordMap) {
        return new HearitAdminResponse(
                hearit.getId(),
                hearit.getTitle(),
                hearit.getSummary(),
                hearit.getOriginalAudioUrl(),
                hearit.getShortAudioUrl(),
                hearit.getScriptUrl(),
                hearit.getSource(),
                hearit.getPlayTime(),
                hearit.getCreatedAt(),
                CategoryInfoResponse.from(hearit.getCategory()),
                keywordMap.getOrDefault(hearit.getId(), List.of())
        );
    }

    public record KeywordInHearit(
            String name
    ) {
    }
}
