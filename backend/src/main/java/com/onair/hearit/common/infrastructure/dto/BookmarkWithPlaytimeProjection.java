package com.onair.hearit.common.infrastructure.dto;

import com.onair.hearit.common.domain.Bookmark;

public interface BookmarkWithPlaytimeProjection {
    Bookmark getBookmark();

    Long getLastPlayTime();
}
