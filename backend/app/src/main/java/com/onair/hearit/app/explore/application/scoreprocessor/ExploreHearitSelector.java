package com.onair.hearit.app.explore.application.scoreprocessor;

import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.infrastructure.jpa.ClusteredHearitRepository;
import com.onair.hearit.core.infrastructure.jpa.HearitRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExploreHearitSelector {

    /**
     * 탐색 추천에서 선호 클러스터와 비선호 클러스터의 후보 비율
     * <p>
     * 70% : 사용자 선호 콘텐츠 (Top3개의 클러스터에서 선정) 30% : 탐색 콘텐츠 (그 외 클러스터에서 선정)
     */
    private static final int MAX_CANDIDATE_HEARIT = 100;
    private static final int PREFERRED_CLUSTER = 3;
    private static final double PREFERRED_RATIO = 0.7; // NON_PREFERRED_RATIO = 0.3

    private final HearitRepository hearitRepository;
    private final ClusteredHearitRepository clusteredHearitRepository;

    public List<Hearit> select(UUID uuid) {
        int targetSize = calculateTargetSize();

        List<Integer> preferredClusters = clusteredHearitRepository.findTopClusterIdsByUser(uuid, PREFERRED_CLUSTER);
        int preferredQuota = (int) (targetSize * PREFERRED_RATIO);
        Set<Long> candidateIds = loadPreferred(preferredClusters, preferredQuota);

        int remainingToFill = targetSize - candidateIds.size();
        if (remainingToFill > 0) {
            candidateIds.addAll(loadExplore(preferredClusters, remainingToFill));
        }
        if (candidateIds.size() < targetSize) {
            candidateIds.addAll(loadFallback(candidateIds, targetSize));
        }
        return hearitRepository.findAllById(candidateIds);
    }

    private int calculateTargetSize() {
        return (int) Math.min(hearitRepository.count(), MAX_CANDIDATE_HEARIT);
    }

    private Set<Long> loadPreferred(List<Integer> clusters, int limit) {
        if (clusters.isEmpty() || limit <= 0) {
            return new HashSet<>();
        }
        return new HashSet<>(clusteredHearitRepository.findRandomHearitIdsByClusters(clusters, limit));
    }

    private Set<Long> loadExplore(List<Integer> preferredClusters, int limit) {
        if (limit <= 0) {
            return new HashSet<>();
        }
        return new HashSet<>(clusteredHearitRepository.findRandomHearitIdsExcludingClusters(preferredClusters, limit));
    }

    private Set<Long> loadFallback(Set<Long> existingIds, int targetSize) {
        int remaining = targetSize - existingIds.size();
        if (remaining <= 0) {
            return new HashSet<>();
        }
        List<Long> fallbackIds = hearitRepository.findRandomIdsExcludingIds(existingIds, remaining);
        return new HashSet<>(fallbackIds);
    }
}
