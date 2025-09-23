package com.onair.hearit.common.infrastructure.dto;

import com.onair.hearit.common.domain.Bookmark;
import com.onair.hearit.common.domain.PlayingHistory;

public interface BookmarkWithPlayingHistoryProjection {
    Bookmark getBookmark();

    PlayingHistory getPlayingHistory();
}
