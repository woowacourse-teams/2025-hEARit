package com.onair.hearit.app.dto.response;

import com.onair.hearit.common.domain.Category;
import com.onair.hearit.common.domain.Hearit;
import java.time.LocalDateTime;

public record RecentlyPlayedHearitResponse(
        Long id,
        String title,
        Integer playTime,
        Long lastPlayTime,
        LocalDateTime createdAt,
        CategoryResponse category
) {
    public static RecentlyPlayedHearitResponse from(Hearit hearit, Long lastPlayTime) {
        return new RecentlyPlayedHearitResponse(
                hearit.getId(),
                hearit.getTitle(),
                hearit.getPlayTime(),
                lastPlayTime,
                hearit.getCreatedAt(),
                CategoryResponse.from(hearit.getCategory())
        );
    }

    private record CategoryResponse(Long id, String name, String colorCode) {
        public static CategoryResponse from(Category category) {
            return new CategoryResponse(category.getId(), category.getName(), category.getColorCode());
        }
    }
}
