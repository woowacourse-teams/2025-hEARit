package com.onair.hearit.app.explore.application;

import com.onair.hearit.app.explore.application.scorefactor.ScoreFactor;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.UserType;
import com.onair.hearit.core.infrastructure.jpa.ClusteredHearitRepository;
import com.onair.hearit.core.infrastructure.jpa.HearitRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExploreScoreCalculator {

    private static final int TARGET_CLUSTER_SIZE = 3;
    private static final int MAX_CANDIDATE_HEARIT = 100; // = 50 + 30 + 20
    private static final int[] FIXED_QUOTAS = {50, 30, 20}; // TOP 1, 2, 3위 Cluster 비중

    private final HearitRepository hearitRepository;
    private final ClusteredHearitRepository clusteredHearitRepository;
    private final List<ScoreFactor> scoreFactors;
    private final ScoreFactorWeightConfig scoreFactorWeight;

    public Map<Long, Double> calculateTotalScores(UUID uuid, UserType userType) {
        List<Hearit> hearits = selectTargetHearits(uuid);
        List<ScoreFactor> supportedScoreFactors = getSupportedScoreFactors(userType);
        Map<Long, Double> totalExploreScores = initTotalExploreScores(hearits);

        for (ScoreFactor scoreFactor : supportedScoreFactors) {
            double weight = scoreFactorWeight.getWeight(scoreFactor.getClass());
            Map<Long, Double> scores = scoreFactor.calculate(uuid, hearits);

            for (Map.Entry<Long, Double> entry : scores.entrySet()) {
                totalExploreScores.merge(
                        entry.getKey(),
                        entry.getValue() * weight,
                        Double::sum);
            }
        }
        return totalExploreScores;
    }

    private List<Hearit> selectTargetHearits(UUID uuid) {
        // 1. 유저 선호 군집을 우선순위로 하여 해당 클러스터 ID를 가져온다.
        List<Integer> targetClusterIds = getTargetClusterIds(uuid);
        Set<Long> targetHearitIds = new HashSet<>();

        // 2. 우선 순위 군집에 해당하는 히어릿을 랜덤으로 가져온다.
        for (int i = 0; i < targetClusterIds.size(); i++) {
            int quota = FIXED_QUOTAS[i];
            int clusterId = targetClusterIds.get(i);
            targetHearitIds.addAll(clusteredHearitRepository.findRandomHearitIdsByCluster(clusterId, quota));
        }

        // 3. 부족한 수량만큼 전체 히어릿에서 랜덤하게 ID를 추출한다.
        if (targetHearitIds.size() < MAX_CANDIDATE_HEARIT) {
            targetHearitIds.addAll(fillMissingHearits(targetHearitIds));
        }
        return hearitRepository.findAllById(targetHearitIds);
    }

    private List<Integer> getTargetClusterIds(UUID uuid) {
        List<Integer> topClusterId = clusteredHearitRepository.findTopClusterIdsByUser(uuid, TARGET_CLUSTER_SIZE);
        Set<Integer> targetClusterIds = new LinkedHashSet<>(topClusterId);

        if (targetClusterIds.size() < TARGET_CLUSTER_SIZE) {
            int neededSize = TARGET_CLUSTER_SIZE - targetClusterIds.size();
            List<Integer> excludedIds = new ArrayList<>(targetClusterIds);
            List<Integer> randomClusters = clusteredHearitRepository.findRandomClusterIdsExcluding(excludedIds,
                    neededSize);
            targetClusterIds.addAll(randomClusters);
        }
        return new ArrayList<>(targetClusterIds);
    }

    private List<Long> fillMissingHearits(Set<Long> currentIds) {
        int neededSize = MAX_CANDIDATE_HEARIT - currentIds.size();
        List<Long> excludedIds = new ArrayList<>(currentIds);
        return hearitRepository.findRandomIdsExcluding(excludedIds, neededSize);
    }

    private List<ScoreFactor> getSupportedScoreFactors(UserType userType) {
        List<ScoreFactor> supportedScoreFactors = new ArrayList<>();
        for (ScoreFactor scoreFactor : scoreFactors) {
            if (scoreFactor.isSupported(userType)) {
                supportedScoreFactors.add(scoreFactor);
            }
        }
        return supportedScoreFactors;
    }

    private Map<Long, Double> initTotalExploreScores(List<Hearit> hearits) {
        return hearits.stream()
                .collect(Collectors.toMap(Hearit::getId, h -> 0.0));
    }
}
