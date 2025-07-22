package com.onair.hearit.dto.response;

import com.onair.hearit.domain.Hearit;
import java.time.LocalDateTime;

public record HearitListResponse(
        Long id,
        String title,
        String summary,
        String source,
        Integer playTime,
        LocalDateTime createdAt
) {

    public static HearitListResponse from(Hearit hearit) {
        return new HearitListResponse(
                hearit.getId(),
                hearit.getTitle(),
                hearit.getSummary(),
                hearit.getSource(),
                hearit.getPlayTime(),
                hearit.getCreatedAt());
    }
}
