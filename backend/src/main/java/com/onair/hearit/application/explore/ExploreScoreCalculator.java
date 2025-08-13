package com.onair.hearit.application.explore;

import com.onair.hearit.application.explore.score.ScoreFactor;
import com.onair.hearit.domain.Hearit;
import java.util.List;
import java.util.Map;


public interface ExploreScoreCalculator {

    Map<Long, Double> calculateTotalScores(Long memberId, List<Hearit> hearits, List<ScoreFactor> scoreFactors);
}
