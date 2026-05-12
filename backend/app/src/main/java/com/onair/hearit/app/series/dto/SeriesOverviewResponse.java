package com.onair.hearit.app.series.dto;

import com.onair.hearit.core.domain.Series;
import java.time.LocalDateTime;

public record SeriesOverviewResponse(
        Long id,
        String title,
        String description,
        String imageUrl,
        LocalDateTime createdAt
) {
    public static SeriesOverviewResponse from(Series series) {
        return new SeriesOverviewResponse(
                series.getId(),
                series.getTitle(),
                series.getDescription(),
                series.getImageUrl(),
                series.getCreatedAt()
        );
    }
}
