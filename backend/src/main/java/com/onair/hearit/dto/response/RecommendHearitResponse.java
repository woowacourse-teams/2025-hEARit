package com.onair.hearit.dto.response;

import com.onair.hearit.domain.Hearit;
import java.time.LocalDateTime;

public record RecommendHearitResponse(
        Long id,
        String title,
        String summary,
        String source,
        Integer playTime,
        LocalDateTime createdAt
) {

    public static RecommendHearitResponse from(Hearit hearit) {
        return new RecommendHearitResponse(
                hearit.getId(),
                hearit.getTitle(),
                hearit.getSummary(),
                hearit.getSource(),
                hearit.getPlayTime(),
                hearit.getCreatedAt());
    }
}
