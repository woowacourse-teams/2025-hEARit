package com.onair.hearit.app.dto.response;

public record BookmarkHearitResponseV1(
        Long hearitId,
        Long bookmarkId,
        String title,
        String summary,
        Integer playTime,
        Long lastPlayTime,
        String categoryColor
) {
    public static BookmarkHearitResponseV1 from(BookmarkHearitResponse bookmarkHearitResponse) {
        BookmarkHearitResponse.CategoryResponse category = bookmarkHearitResponse.category();
        return new BookmarkHearitResponseV1(
                bookmarkHearitResponse.hearitId(),
                bookmarkHearitResponse.bookmarkId(),
                bookmarkHearitResponse.title(),
                bookmarkHearitResponse.summary(),
                bookmarkHearitResponse.playTime(),
                bookmarkHearitResponse.lastPlayTime(),
                category.colorCode()
        );
    }
}
