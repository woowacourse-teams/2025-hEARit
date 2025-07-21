package com.onair.hearit.dto.response;

import com.onair.hearit.domain.Hearit;
import java.util.List;
import org.springframework.data.domain.Page;

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

    public static List<HearitSearchResponse> from(Page<Hearit> hearits) {
        return hearits.stream()
                .map(HearitSearchResponse::from)
                .toList();
    }
}
