package com.onair.hearit.application;

import com.onair.hearit.common.exception.custom.InvalidInputException;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.RecommendHearit;
import com.onair.hearit.infrastructure.HearitRepository;
import com.onair.hearit.infrastructure.RecommendHearitRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DbRecommendHearitProvider implements RecommendHearitProvider {

    private final HearitRepository hearitRepository;
    private final RecommendHearitRepository recommendHearitRepository;

    @Override
    public List<Hearit> getRecommendHearit(int hearitCount) {
        List<RecommendHearit> recommendHearits = getRecommendHearits(hearitCount);
        return recommendHearits.stream()
                .map(RecommendHearit::getHearit)
                .toList();
    }

    private List<RecommendHearit> getRecommendHearits(int hearitCount) {
        List<RecommendHearit> recommendHearits = recommendHearitRepository.findByRecentRecommendDateLimitN(
                LocalDate.now(), hearitCount);
        if (recommendHearits.size() < hearitCount) {
            throw new InvalidInputException(
                    "추천할 히어릿이 부족합니다. requestSize:" + hearitCount + ", currentSize: " + recommendHearits.size());
        }
        return recommendHearits;
    }
}
