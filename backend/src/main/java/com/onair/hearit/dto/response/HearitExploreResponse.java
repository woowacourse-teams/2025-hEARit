package com.onair.hearit.dto.response;

import com.onair.hearit.domain.Hearit;

public record HearitExploreResponse(
        Long id,
        String title,
        String summary) {

    public static HearitExploreResponse from(Hearit hearit) {
        return new HearitExploreResponse(
                hearit.getId(),
                hearit.getTitle(),
                hearit.getSummary()
        );
    }
}
