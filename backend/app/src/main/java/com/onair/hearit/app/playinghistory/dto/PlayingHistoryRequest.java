package com.onair.hearit.app.playinghistory.dto;

import com.onair.hearit.app.exception.custom.InvalidInputException;
import jakarta.validation.constraints.NotNull;

public record PlayingHistoryRequest(
        @NotNull Long hearitId,
        @NotNull Long lastPlayTime,
        Long clientEventTime // 하휘 호환성을 위해 nullable 허용 (null: 서버 수신 시간 자동 추가)
) {
    public PlayingHistoryRequest {
        if (lastPlayTime < 0) {
            throw new InvalidInputException("마지막 재생 기록은 0 이상이어야 합니다.");
        }
        if (clientEventTime != null && clientEventTime < 0) {
            throw new InvalidInputException("요청 시간은 0 이상이어야 합니다.");
        }
    }
}
