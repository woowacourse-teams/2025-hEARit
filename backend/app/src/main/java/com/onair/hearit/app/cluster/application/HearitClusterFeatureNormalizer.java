package com.onair.hearit.app.cluster.application;

import com.onair.hearit.app.cluster.dto.NormalizedHearitClusterFeature;
import com.onair.hearit.core.domain.HearitCluster;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class HearitClusterFeatureNormalizer {

    private static final double RECENCY_LAMBDA = 0.05;

    public List<NormalizedHearitClusterFeature> normalizeFromEntities(List<HearitCluster> entities) {
        double maxView = entities.stream().mapToLong(HearitCluster::getViewCount).max().orElse(1L);
        double maxLike = entities.stream().mapToLong(HearitCluster::getLikeCount).max().orElse(1L);
        double maxBookmark = entities.stream().mapToLong(HearitCluster::getBookmarkCount).max().orElse(1L);
        double maxPlayTime = entities.stream().mapToDouble(HearitCluster::getAvgPlayTime).max().orElse(1.0);

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
        long days = Duration.between(createdAt, LocalDateTime.now()).toDays();
        return Math.exp(-RECENCY_LAMBDA * days);
    }
}
