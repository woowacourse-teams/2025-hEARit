package com.onair.hearit.app.recommendhearit.application;

import com.onair.hearit.app.recommendhearit.application.strategy.RecommendHearitStrategy;
import com.onair.hearit.app.recommendhearit.dto.RecommendHearitResponse;
import com.onair.hearit.core.domain.Hearit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecommendHearitService {

    private static final int RECOMMEND_HEARIT_COUNT = 5;

    private final RecommendHearitStrategy recommendHearitStrategy;

    public List<RecommendHearitResponse> getRecommendedHearits() {
        List<Hearit> recommendHearits = recommendHearitStrategy.getRecommendHearit(RECOMMEND_HEARIT_COUNT);
        return recommendHearits.stream()
                .map(RecommendHearitResponse::from)
                .toList();
    }
}
