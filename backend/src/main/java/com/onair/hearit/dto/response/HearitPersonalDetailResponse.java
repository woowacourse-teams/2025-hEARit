package com.onair.hearit.dto.response;

import com.onair.hearit.domain.Hearit;
import java.time.LocalDateTime;

public record HearitPersonalDetailResponse(
        Long id,
        String title,
        String summary,
        String source,
        Integer playTime,
        LocalDateTime createdAt,
        Boolean isBookmarked
) {

    public static HearitPersonalDetailResponse of(Hearit hearit, Boolean isBookmarked) {
        return new HearitPersonalDetailResponse(
                hearit.getId(),
                hearit.getTitle(),
                hearit.getSummary(),
                hearit.getSource(),
                hearit.getPlayTime(),
                hearit.getCreatedAt(),
                isBookmarked);
    }
}
