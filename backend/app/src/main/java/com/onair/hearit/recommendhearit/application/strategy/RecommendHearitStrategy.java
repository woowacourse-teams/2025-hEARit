package com.onair.hearit.recommendhearit.application.strategy;

import com.onair.hearit.domain.Hearit;
import java.util.List;

public interface RecommendHearitStrategy {

    List<Hearit> getRecommendHearit(int hearitCount);
}
