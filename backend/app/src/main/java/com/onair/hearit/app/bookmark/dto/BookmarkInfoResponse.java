package com.onair.hearit.app.bookmark.dto;

import com.onair.hearit.core.domain.Bookmark;

public record BookmarkInfoResponse(
        Long id
) {
    public static BookmarkInfoResponse from(Bookmark bookmark) {
        return new BookmarkInfoResponse(bookmark.getId());
    }
}
