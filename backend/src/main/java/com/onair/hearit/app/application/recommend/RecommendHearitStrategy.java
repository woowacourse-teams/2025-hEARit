package com.onair.hearit.app.application.recommend;

import com.onair.hearit.common.domain.Hearit;
import java.util.List;

public interface RecommendHearitStrategy {

    List<Hearit> getRecommendHearit(int hearitCount);
}
