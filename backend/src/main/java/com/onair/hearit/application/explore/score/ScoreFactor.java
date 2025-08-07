package com.onair.hearit.application.explore.score;

import com.onair.hearit.domain.Hearit;
import java.util.List;
import java.util.Map;

public interface ScoreFactor {

    Map<Long, Double> calculate(Long memberId, List<Hearit> hearits);
}
