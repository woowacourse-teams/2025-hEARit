package com.onair.hearit.app.application.explore.scorefactor;

import com.onair.hearit.auth.domain.UserType;
import com.onair.hearit.common.domain.Hearit;
import java.time.Duration;
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
    public boolean isSupported(UserType userType) {
        return userType == UserType.GUEST || userType == UserType.MEMBER;
    }

    @Override
    public Map<Long, Double> calculate(String uuid, List<Hearit> hearits) {
        return hearits.stream()
                .collect(Collectors.toMap(
                        Hearit::getId,
                        hearit -> {
                            long daysFromCreatedAt = Duration.between(hearit.getCreatedAt(), LocalDateTime.now())
                                    .toDays();
                            double score = MAX_RECENCY_SCORE - (daysFromCreatedAt / DAYS_PER_POINT);
                            return Math.max(MIN_RECENCY_SCORE, score);
                        }
                ));
    }
}
