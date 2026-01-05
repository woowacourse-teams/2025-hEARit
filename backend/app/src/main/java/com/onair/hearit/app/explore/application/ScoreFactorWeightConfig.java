package com.onair.hearit.app.explore.application;

import com.onair.hearit.app.explore.application.scorefactor.BookmarkScoreFactor;
import com.onair.hearit.app.explore.application.scorefactor.RandomScoreFactor;
import com.onair.hearit.app.explore.application.scorefactor.RecencyScoreFactor;
import com.onair.hearit.app.explore.application.scorefactor.ScoreFactor;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScoreFactorWeightConfig {

    private final Map<Class<? extends ScoreFactor>, Double> weights = Map.of(
            RecencyScoreFactor.class, 0.2,
            BookmarkScoreFactor.class, 0.12,
            RandomScoreFactor.class, 0.2
    );

    public double getWeight(Class<? extends ScoreFactor> factorClass) {
        return weights.getOrDefault(factorClass, 0.0);
    }
}
