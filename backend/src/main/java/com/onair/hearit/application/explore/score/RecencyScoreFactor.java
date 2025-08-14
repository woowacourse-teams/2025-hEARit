package com.onair.hearit.application.explore.score;

import com.onair.hearit.domain.Hearit;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class RecencyScoreFactor implements ScoreFactor {

    private static final double MAX_RECENCY_SCORE = 20.0;
    private static final double MIN_RECENCY_SCORE = 0.0;
    private static final double DAYS_PER_POINT = 2.0;

    @Override
    public Map<Long, Double> calculate(Long memberId, List<Hearit> hearits) {
        return hearits.stream()
                .collect(Collectors.toMap(
                        Hearit::getId,
                        hearit -> {
                            long daysFromCreatedAt = Duration.between(hearit.getCreatedAt(), LocalDateTime.now()).toDays();
                            double score = MAX_RECENCY_SCORE - (daysFromCreatedAt / DAYS_PER_POINT);
                            return Math.max(MIN_RECENCY_SCORE, score);
                        }
                ));
    }
}
