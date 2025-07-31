package com.onair.hearit.dto.response;

import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import java.time.LocalDateTime;
import java.util.List;

public record GroupedHearitsWithCategoryResponse(
        Long categoryId,
        String categoryName,
        String colorCode,
        List<HearitResponse> hearits
) {

    public static GroupedHearitsWithCategoryResponse from(Category category, List<Hearit> hearits) {
        return new GroupedHearitsWithCategoryResponse(
                category.getId(),
                category.getName(),
                category.getColorCode(),
                mapToHearitResponses(hearits)
        );
    }

    private static List<HearitResponse> mapToHearitResponses(final List<Hearit> hearits) {
        return hearits.stream()
                .map(HearitResponse::from)
                .toList();
    }

    private record HearitResponse(
            Long hearitId,
            String title,
            LocalDateTime createdAt
    ) {
        public static HearitResponse from(Hearit hearit) {
            return new HearitResponse(
                    hearit.getId(),
                    hearit.getTitle(),
                    hearit.getCreatedAt()
            );
        }
    }
}
