package com.onair.hearit.dto.response;

import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import java.time.LocalDateTime;
import java.util.List;

public record HomeCategoryHearitResponse(
        Long categoryId,
        String categoryName,
        String colorCode,
        List<CategoryHearitResponse> hearits
) {

    public static HomeCategoryHearitResponse from(Category category, List<Hearit> hearits) {
        return new HomeCategoryHearitResponse(
                category.getId(),
                category.getName(),
                category.getColorCode(),
                getHearits(hearits)
        );
    }

    private static List<CategoryHearitResponse> getHearits(final List<Hearit> hearits) {
        return hearits.stream()
                .map(CategoryHearitResponse::from)
                .toList();
    }

    public record CategoryHearitResponse(
            Long hearitId,
            String title,
            LocalDateTime createdAt,
            String categoryName,
            String categoryColor
    ) {
        public static CategoryHearitResponse from(Hearit hearit) {
            return new CategoryHearitResponse(
                    hearit.getId(),
                    hearit.getTitle(),
                    hearit.getCreatedAt(),
                    hearit.getCategory().getName(),
                    hearit.getCategory().getColorCode()
            );
        }
    }
}
