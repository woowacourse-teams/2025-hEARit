package com.onair.hearit.app.bookmark.dto;

import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.Hearit;
import java.time.LocalDateTime;

public record BookmarkUnfinishedHearitResponse(
        Long id,
        String title,
        Integer playTime,
        Long lastPlayTime,
        LocalDateTime createdAt,
        CategoryResponse category
) {
    public static BookmarkUnfinishedHearitResponse from(Hearit hearit, Long lastPlayTime) {
        return new BookmarkUnfinishedHearitResponse(
                hearit.getId(),
                hearit.getTitle(),
                hearit.getPlayTime(),
                lastPlayTime,
                hearit.getCreatedAt(),
                CategoryResponse.from(hearit.getCategory())
        );
    }

    public record CategoryResponse(Long id, String name, String colorCode) {
        public static CategoryResponse from(Category category) {
            return new CategoryResponse(category.getId(), category.getName(), category.getColorCode());
        }
    }
}
