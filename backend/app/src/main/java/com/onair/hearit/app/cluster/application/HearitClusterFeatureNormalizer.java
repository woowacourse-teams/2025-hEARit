package com.onair.hearit.app.cluster.application;

import com.onair.hearit.app.cluster.dto.NormalizedHearitClusterFeature;
import com.onair.hearit.core.domain.HearitCluster;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class HearitClusterFeatureNormalizer {

    private static final double MIN_NORMALIZATION_THRESHOLD = 1.0;
    private static final double RECENCY_LAMBDA = 0.05;

    public List<NormalizedHearitClusterFeature> normalizeFromEntities(List<HearitCluster> entities) {
        double maxView = Math.max(entities.stream().mapToLong(HearitCluster::getViewCount).max().orElse(0L),
                MIN_NORMALIZATION_THRESHOLD);
        double maxLike = Math.max(entities.stream().mapToLong(HearitCluster::getLikeCount).max().orElse(0L),
                MIN_NORMALIZATION_THRESHOLD);
        double maxBookmark = Math.max(entities.stream().mapToLong(HearitCluster::getBookmarkCount).max().orElse(0L),
                MIN_NORMALIZATION_THRESHOLD);
        double maxPlayTime = Math.max(entities.stream().mapToDouble(HearitCluster::getAvgPlayTime).max().orElse(0.0),
                MIN_NORMALIZATION_THRESHOLD);

        return entities.stream()
                .map(e -> new NormalizedHearitClusterFeature(
                        e.getHearitId(),
                        (double) e.getViewCount() / maxView,
                        (double) e.getLikeCount() / maxLike,
                        (double) e.getBookmarkCount() / maxBookmark,
                        e.getAvgPlayTime() / maxPlayTime,
                        e.getCompletionRate(),
                        calculateRecency(e.getCreatedAt())
                )).toList();
    }

    private double calculateRecency(LocalDateTime createdAt) {
        long days = Math.max(0, Duration.between(createdAt, LocalDateTime.now()).toDays());
        return Math.exp(-RECENCY_LAMBDA * days);
    }
}
