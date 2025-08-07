package com.onair.hearit.admin.dto.response;

import com.onair.hearit.domain.Hearit;
import java.time.LocalDate;

public record RecommendHearitResponse(
        Long id,
        String title,
        String summary,
        Integer playTime,
        String categoryName,
        LocalDate createdAt,
        LocalDate lastRecommendedDate
) {

    public static RecommendHearitResponse of(Hearit hearit, LocalDate lastRecommendedDate) {
        return new RecommendHearitResponse(
                hearit.getId(),
                hearit.getTitle(),
                hearit.getSummary(),
                hearit.getPlayTime(),
                hearit.getCategory().getName(),
                LocalDate.from(hearit.getCreatedAt()),
                lastRecommendedDate
        );
    }
}
