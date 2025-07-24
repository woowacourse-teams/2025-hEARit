package com.onair.hearit.dto.response;

import com.onair.hearit.domain.Bookmark;
import com.onair.hearit.domain.Hearit;

public record BookmarkHearitResponse(
        Long hearitId,
        Long bookmarkId,
        String title,
        String summary,
        Integer playTime
) {
    public static BookmarkHearitResponse of(Bookmark bookmark, Hearit hearit) {
        return new BookmarkHearitResponse(
                hearit.getId(),
                bookmark.getId(),
                hearit.getTitle(),
                hearit.getSummary(),
                hearit.getPlayTime());
    }
}
