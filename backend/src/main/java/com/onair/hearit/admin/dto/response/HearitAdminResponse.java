package com.onair.hearit.admin.dto.response;

import com.onair.hearit.domain.Hearit;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record HearitAdminResponse(
        Long id,
        String title,
        String summary,
        String source,
        Integer playTime,
        LocalDateTime createdAt,
        List<KeywordInHearit> keywords
) {
    public record KeywordInHearit(
            String name
    ) {
    }

    public static HearitAdminResponse from(Hearit hearit, Map<Long, List<KeywordInHearit>> keywordMap) {
        return new HearitAdminResponse(
                hearit.getId(),
                hearit.getTitle(),
                hearit.getSummary(),
                hearit.getSource(),
                hearit.getPlayTime(),
                hearit.getCreatedAt(),
                keywordMap.getOrDefault(hearit.getId(), List.of())
        );
    }
}
