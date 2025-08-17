package com.onair.hearit.app.dto.response;

import com.onair.hearit.common.domain.Bookmark;

public record BookmarkInfoResponse(
        Long id
) {
    public static BookmarkInfoResponse from(Bookmark bookmark) {
        return new BookmarkInfoResponse(bookmark.getId());
    }
}
