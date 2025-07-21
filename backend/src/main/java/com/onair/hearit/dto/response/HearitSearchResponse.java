package com.onair.hearit.dto.response;

import com.onair.hearit.domain.Hearit;

public record HearitSearchResponse(
        Long id,
        String title,
        String summary,
        Integer playTime
) {

    public static HearitSearchResponse from(Hearit hearit) {
        return new HearitSearchResponse(
                hearit.getId(),
                hearit.getTitle(),
                hearit.getSummary(),
                hearit.getPlayTime()
        );
    }
}
