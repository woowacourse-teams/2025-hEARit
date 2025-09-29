package com.onair.hearit.app.recommendhearit.application.strategy;

import com.onair.hearit.core.domain.Hearit;
import java.util.List;

public interface RecommendHearitStrategy {

    List<Hearit> getRecommendHearit(int hearitCount);
}
