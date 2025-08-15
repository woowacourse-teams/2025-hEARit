package com.onair.hearit.admin.dto.response;

import com.onair.hearit.domain.Hearit;
import java.time.LocalDate;

public record AdminRecommendHearitResponse(
        Long id,
        String title,
        String summary,
        Integer playTime,
        String categoryName,
        LocalDate createdAt,
        LocalDate lastRecommendedDate
) {
    public static AdminRecommendHearitResponse of(Hearit hearit, LocalDate lastRecommendedDate) {
        return new AdminRecommendHearitResponse(
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
