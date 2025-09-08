package com.onair.hearit.app.dto.response;

import com.onair.hearit.common.domain.Bookmark;
import com.onair.hearit.common.domain.Category;
import com.onair.hearit.common.domain.Hearit;

public record BookmarkHearitResponse(
        Long hearitId,
        Long bookmarkId,
        String title,
        String summary,
        Integer playTime,
        CategoryResponse category
) {
    public static BookmarkHearitResponse of(Bookmark bookmark, Hearit hearit) {
        return new BookmarkHearitResponse(
                hearit.getId(),
                bookmark.getId(),
                hearit.getTitle(),
                hearit.getSummary(),
                hearit.getPlayTime(),
                CategoryResponse.from(hearit.getCategory()));
    }

    private record CategoryResponse(Long id, String name, String colorCode) {
        public static CategoryResponse from(Category category) {
            return new CategoryResponse(category.getId(), category.getName(), category.getColorCode());
        }
    }
}
