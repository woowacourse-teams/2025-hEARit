package com.onair.hearit.infrastructure.projection;

import com.onair.hearit.domain.Bookmark;
import com.onair.hearit.domain.PlayingHistory;

public interface BookmarkWithPlayingHistoryProjection {
    Bookmark getBookmark();

    PlayingHistory getPlayingHistory();
}
