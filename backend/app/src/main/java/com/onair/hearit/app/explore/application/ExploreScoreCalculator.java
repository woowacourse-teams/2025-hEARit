package com.onair.hearit.app.explore.application;

import com.onair.hearit.app.explore.application.scorefactor.ScoreFactor;
import com.onair.hearit.app.explore.application.scoreprocessor.ExploreHearitSelector;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.UserType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExploreScoreCalculator {

    private final ExploreHearitSelector exploreHearitSelector;
    private final List<ScoreFactor> scoreFactors;
    private final ScoreFactorWeightConfig scoreFactorWeight;

    public Map<Long, Double> calculateTotalScores(UUID uuid, UserType userType) {
        List<Hearit> hearits = exploreHearitSelector.select(uuid);
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
