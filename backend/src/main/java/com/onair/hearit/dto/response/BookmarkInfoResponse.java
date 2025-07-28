package com.onair.hearit.dto.response;

import com.onair.hearit.domain.Bookmark;

public record BookmarkInfoResponse(
        Long id
) {
    public static BookmarkInfoResponse from(Bookmark bookmark) {
        return new BookmarkInfoResponse(bookmark.getId());
    }
}
