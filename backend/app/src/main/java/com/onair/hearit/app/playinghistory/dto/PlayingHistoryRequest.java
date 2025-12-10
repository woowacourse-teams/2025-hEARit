package com.onair.hearit.app.playinghistory.dto;

import com.onair.hearit.app.exception.custom.InvalidInputException;

public record PlayingHistoryRequest(
        Long hearitId,
        Long lastPlayTime,
        Long clientEventTime
) {
    public PlayingHistoryRequest {
        if (hearitId == null) {
            throw new InvalidInputException("히어릿은 null이 될 수 없습니다.");
        }
        if (lastPlayTime == null || lastPlayTime < 0) {
            throw new InvalidInputException("마지막 재생 기록은 0 이상이어야 합니다.");
        }
        if (clientEventTime != null && clientEventTime < 0) {
            throw new InvalidInputException("요청 시간은 0 이상이어야 합니다.");
        }
    }
}
