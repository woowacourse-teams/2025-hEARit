package com.onair.hearit.application;

import com.onair.hearit.domain.Hearit;
import java.util.List;

public interface FixedRecommendHearitStrategy {

    List<Hearit> getRecommendHearit(int hearitCount);
}
