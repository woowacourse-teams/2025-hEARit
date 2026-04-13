package com.onair.hearit.app.series.dto;

import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.Series;
import java.time.LocalDateTime;
import java.util.List;

public record SeriesDetailResponse(
        Long id,
        String title,
        String description,
        String imageUrl,
        LocalDateTime createdAt,
        List<SeriesHearitResponse> hearits
) {
    public static SeriesDetailResponse from(Series series, List<Hearit> hearits) {
        List<SeriesHearitResponse> hearitResponses = hearits.stream()
                .map(SeriesHearitResponse::from)
                .toList();
        return new SeriesDetailResponse(
                series.getId(),
                series.getTitle(),
                series.getDescription(),
                series.getImageUrl(),
                series.getCreatedAt(),
                hearitResponses
        );
    }

    public record SeriesHearitResponse(
            Long id,
            String title,
            Integer playTime,
            LocalDateTime createdAt
    ) {
        public static SeriesHearitResponse from(Hearit hearit) {
            return new SeriesHearitResponse(
                    hearit.getId(),
                    hearit.getTitle(),
                    hearit.getPlayTime(),
                    hearit.getCreatedAt()
            );
        }
    }
}
