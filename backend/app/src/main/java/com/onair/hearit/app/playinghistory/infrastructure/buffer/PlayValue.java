package com.onair.hearit.app.playinghistory.infrastructure.buffer;

import com.onair.hearit.core.domain.PlayingHistory;

public record PlayValue(
            String userUuid,
            long hearitId,
            long lastPlayTime,
            long clientEventTime
    ) {
        public static PlayValue from(PlayingHistory history, long clientEventTime) {
            return new PlayValue(
                    history.getUserUuid(),
                    history.getHearitId(),
                    history.getLastPlayTime(),
                    clientEventTime
            );
        }

        public boolean isMoreRecentThan(PlayValue other) {
            return other != null && this.clientEventTime > other.clientEventTime();
        }
    }
