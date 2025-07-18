package com.onair.hearit.dto.response;

import com.onair.hearit.domain.Hearit;

public record HearitSimpleResponse(
        Long id,
        String title,
        String summary,
        Integer playTime
) {

    public static HearitSimpleResponse from(Hearit hearit) {
        return new HearitSimpleResponse(
                hearit.getId(),
                hearit.getTitle(),
                hearit.getSummary(),
                hearit.getPlayTime()
        );
    }
}
