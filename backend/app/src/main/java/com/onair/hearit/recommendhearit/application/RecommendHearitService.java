package com.onair.hearit.recommendhearit.application;

import com.onair.hearit.domain.Hearit;
import com.onair.hearit.recommendhearit.application.strategy.RecommendHearitStrategy;
import com.onair.hearit.recommendhearit.dto.RecommendHearitResponse;
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
