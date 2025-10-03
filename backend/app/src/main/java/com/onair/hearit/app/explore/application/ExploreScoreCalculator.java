package com.onair.hearit.app.explore.application;

import com.onair.hearit.app.explore.application.scorefactor.ScoreFactor;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.UserType;
import com.onair.hearit.core.infrastructure.jpa.HearitRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExploreScoreCalculator {

    private final HearitRepository hearitRepository;
    private final List<ScoreFactor> scoreFactors;

    public Map<Long, Double> calculateTotalScores(String uuid, UserType userType) {
        List<ScoreFactor> supportedScoreFactors = getSupportedScoreFactors(userType);
        List<Hearit> hearits = hearitRepository.findAll(Pageable.ofSize(100)).getContent();
        Map<Long, Double> totalExploreScores = initTotalExploreScores(hearits);
        for (ScoreFactor scoreFactor : supportedScoreFactors) {
            Map<Long, Double> scores = scoreFactor.calculate(uuid, hearits);
            for (Map.Entry<Long, Double> entry : scores.entrySet()) {
                totalExploreScores.merge(entry.getKey(), entry.getValue(), Double::sum);
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
