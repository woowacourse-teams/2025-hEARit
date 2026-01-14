package com.onair.hearit.app.playinghistory.infrastructure.buffer;

import com.onair.hearit.core.domain.PlayingHistory;
import java.util.UUID;

public record PlayHistoryValue(
        UUID userUuid,
        long hearitId,
        long lastPlayTime,
        long clientEventTime
) {
    public static PlayHistoryValue from(PlayingHistory history, long clientEventTime) {
        return new PlayHistoryValue(
                history.getUserUuid(),
                history.getHearitId(),
                history.getLastPlayTime(),
                clientEventTime
        );
    }

    public boolean isMoreRecentThan(PlayHistoryValue other) {
        return other != null && this.clientEventTime > other.clientEventTime();
    }
}
