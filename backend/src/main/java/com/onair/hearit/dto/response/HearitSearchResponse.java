package com.onair.hearit.dto.response;

import com.onair.hearit.domain.Hearit;
import org.springframework.data.domain.Page;

import java.util.List;

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

    public static List<HearitSearchResponse> from(Page<com.onair.hearit.domain.Hearit> hearits) {
        return hearits.stream()
                .map(HearitSearchResponse::from)
                .toList();
    }
}
