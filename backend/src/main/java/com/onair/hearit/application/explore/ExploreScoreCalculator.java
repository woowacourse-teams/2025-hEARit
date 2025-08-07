package com.onair.hearit.application.explore;

import com.onair.hearit.application.explore.score.ScoreFactor;
import com.onair.hearit.domain.Hearit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExploreScoreCalculator {

    public Map<Long, Double> calculateTotalScores(Long memberId, List<Hearit> hearits, List<ScoreFactor> scoreFactors) {
        Map<Long, Double> totalScoreMap = hearits.stream()
                .collect(Collectors.toMap(Hearit::getId, h -> 0.0));

        for (ScoreFactor scoreFactor : scoreFactors) {
            Map<Long, Double> factorScores = scoreFactor.calculate(memberId, hearits);
            for (Map.Entry<Long, Double> entry : factorScores.entrySet()) {
                totalScoreMap.merge(entry.getKey(), entry.getValue(), Double::sum);
            }
        }

        return totalScoreMap;
    }
}
