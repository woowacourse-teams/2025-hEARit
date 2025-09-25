package com.onair.hearit.application.recommend;

import com.onair.hearit.domain.Hearit;
import java.util.List;

public interface RecommendHearitStrategy {

    List<Hearit> getRecommendHearit(int hearitCount);
}
