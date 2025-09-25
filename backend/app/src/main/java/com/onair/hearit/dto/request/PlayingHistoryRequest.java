package com.onair.hearit.dto.request;

import com.onair.hearit.exception.custom.InvalidInputException;

public record PlayingHistoryRequest(
        Long hearitId,
        Long lastPlayTime
) {
    public PlayingHistoryRequest {
        if (hearitId == null) {
            throw new InvalidInputException("히어릿은 null이 될 수 없습니다.");
        }
        if (lastPlayTime == null || lastPlayTime < 0) {
            throw new InvalidInputException("마지막 재생 기록은 0 이상이어야 합니다.");
        }
    }
}
