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

    private static final double RECENCY_EXPIRE_DAYS = 60.0;

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
        return Math.clamp(1.0 - (daysPassed / RECENCY_EXPIRE_DAYS), 0, 1.0);
    }
}
