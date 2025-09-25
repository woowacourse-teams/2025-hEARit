package com.onair.hearit.dto.response;

public record BookmarkHearitResponseV1(
        Long hearitId,
        Long bookmarkId,
        String title,
        String summary,
        Integer playTime,
        Long lastPlayTime,
        String categoryColor
) {
    public static BookmarkHearitResponseV1 from(BookmarkHearitResponseV2 bookmarkHearitResponseV2) {
        BookmarkHearitResponseV2.CategoryResponse category = bookmarkHearitResponseV2.category();
        return new BookmarkHearitResponseV1(
                bookmarkHearitResponseV2.hearitId(),
                bookmarkHearitResponseV2.bookmarkId(),
                bookmarkHearitResponseV2.title(),
                bookmarkHearitResponseV2.summary(),
                bookmarkHearitResponseV2.playTime(),
                bookmarkHearitResponseV2.lastPlayTime(),
                category.colorCode()
        );
    }
}
