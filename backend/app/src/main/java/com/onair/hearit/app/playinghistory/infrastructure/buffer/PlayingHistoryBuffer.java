package com.onair.hearit.app.playinghistory.infrastructure.buffer;

import com.onair.hearit.core.domain.PlayingHistory;

public interface PlayingHistoryBuffer {

    void add(PlayingHistory playingHistory);

    void flush();
}
