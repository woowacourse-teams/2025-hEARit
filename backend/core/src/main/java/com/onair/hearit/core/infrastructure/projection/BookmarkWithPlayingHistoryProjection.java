package com.onair.hearit.core.infrastructure.projection;

import com.onair.hearit.core.domain.Bookmark;
import com.onair.hearit.core.domain.PlayingHistory;

public interface BookmarkWithPlayingHistoryProjection {

    Bookmark getBookmark();

    PlayingHistory getPlayingHistory();
}
