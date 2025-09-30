package com.onair.hearit.app.hearit.dto;

import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.Hearit;
import java.time.LocalDateTime;
import java.util.List;

public record HearitsWithRecommendCategoryResponse(
        Long categoryId,
        String categoryName,
        String colorCode,
        List<HearitResponse> hearits
) {
    public static HearitsWithRecommendCategoryResponse from(Category category, List<Hearit> hearits) {
        return new HearitsWithRecommendCategoryResponse(
                category.getId(),
                category.getName(),
                category.getColorCode(),
                mapToHearitResponses(hearits)
        );
    }

    private static List<HearitResponse> mapToHearitResponses(List<Hearit> hearits) {
        return hearits.stream()
                .map(HearitResponse::from)
                .toList();
    }

    public record HearitResponse(
            Long hearitId,
            String title,
            LocalDateTime createdAt
    ) {
        private static HearitResponse from(Hearit hearit) {
            return new HearitResponse(
                    hearit.getId(),
                    hearit.getTitle(),
                    hearit.getCreatedAt()
            );
        }
    }
}
