package com.onair.hearit.app.dto.response;

import com.onair.hearit.common.domain.Hearit;
import java.time.LocalDateTime;

public record RecommendHearitResponse(
        Long id,
        String title,
        Integer playTime,
        LocalDateTime createdAt,
        String categoryName,
        String categoryColor
) {
    public static RecommendHearitResponse from(Hearit hearit) {
        return new RecommendHearitResponse(
                hearit.getId(),
                hearit.getTitle(),
                hearit.getPlayTime(),
                hearit.getCreatedAt(),
                hearit.getCategory().getName(),
                hearit.getCategory().getColorCode());
    }
}
