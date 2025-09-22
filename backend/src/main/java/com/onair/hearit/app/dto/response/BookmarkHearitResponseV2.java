package com.onair.hearit.app.dto.response;

import com.onair.hearit.common.domain.Bookmark;
import com.onair.hearit.common.domain.Category;
import com.onair.hearit.common.domain.Hearit;

public record BookmarkHearitResponseV2(
        Long hearitId,
        Long bookmarkId,
        String title,
        String summary,
        Integer playTime,
        Long lastPlayTime,
        CategoryResponse category
) {
    public static BookmarkHearitResponseV2 of(Bookmark bookmark, Hearit hearit, Long lastPlayTime) {
        return new BookmarkHearitResponseV2(
                hearit.getId(),
                bookmark.getId(),
                hearit.getTitle(),
                hearit.getSummary(),
                hearit.getPlayTime(),
                lastPlayTime,
                CategoryResponse.from(hearit.getCategory()));
    }

    public record CategoryResponse(Long id, String name, String colorCode) {
        public static CategoryResponse from(Category category) {
            return new CategoryResponse(category.getId(), category.getName(), category.getColorCode());
        }
    }
}
