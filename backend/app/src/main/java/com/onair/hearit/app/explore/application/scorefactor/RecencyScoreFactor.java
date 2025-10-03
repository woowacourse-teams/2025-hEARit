package com.onair.hearit.app.explore.application.scorefactor;

import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.UserType;
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
    private static final double POINT_LOSS_PER_DAY = 0.5;

    @Override
    public boolean isSupported(UserType userType) {
        return userType == UserType.GUEST || userType == UserType.MEMBER;
    }

    @Override
    public Map<Long, Double> calculate(String ignored, List<Hearit> hearits) {
        LocalDateTime now = LocalDateTime.now();
        return hearits.stream()
                .collect(Collectors.toMap(
                        Hearit::getId,
                        hearit -> calculateRecencyScore(hearit, now)
                ));
    }

    private double calculateRecencyScore(Hearit hearit, LocalDateTime now) {
        Duration duration = Duration.between(hearit.getCreatedAt(), now);
        long daysPassed = duration.toDays();
        double score = MAX_RECENCY_SCORE - (daysPassed * POINT_LOSS_PER_DAY);
        return Math.max(MIN_RECENCY_SCORE, score);
    }
}
