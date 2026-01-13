package com.onair.hearit.app.explore.application;

import com.onair.hearit.app.explore.application.scorefactor.BookmarkScoreFactor;
import com.onair.hearit.app.explore.application.scorefactor.PlayingHistoryScoreFactor;
import com.onair.hearit.app.explore.application.scorefactor.RandomScoreFactor;
import com.onair.hearit.app.explore.application.scorefactor.RecencyScoreFactor;
import com.onair.hearit.app.explore.application.scorefactor.ScoreFactor;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.aop.support.AopUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScoreFactorWeightConfig {

    private final List<ScoreFactor> scoreFactors;

    private final Map<Class<? extends ScoreFactor>, Double> weights = Map.of(
            RecencyScoreFactor.class, 0.2,
            RandomScoreFactor.class, 0.2,
            BookmarkScoreFactor.class, 0.05,
            PlayingHistoryScoreFactor.class, 0.05
    );

    // 모든 ScoreFactor가 weights에 등록되었는지 확인
    @PostConstruct
    void validateWeightsRegistered() {
        for (ScoreFactor factor : scoreFactors) {
            Class<?> targetClass = AopUtils.getTargetClass(factor);
            @SuppressWarnings("unchecked")
            Class<? extends ScoreFactor> factorClass = (Class<? extends ScoreFactor>) targetClass;

            if (!weights.containsKey(factorClass)) {
                throw new IllegalStateException(
                        "ScoreFactorWeightConfig에 weight가 등록되지 않았습니다. : " + factorClass.getName());
            }
        }
    }

    public double getWeight(Class<? extends ScoreFactor> factorClass) {
        if (weights.containsKey(factorClass)) {
            return weights.get(factorClass);
        }

        throw new IllegalArgumentException("ScoreFactor가 ScoreFactorWeightConfig에 없습니다. : " + factorClass.getName());
    }
}
