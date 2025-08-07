package com.onair.hearit.application.explore.score;

import com.onair.hearit.domain.Hearit;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class RecencyScoreFactor implements ScoreFactor {

    @Override
    public Map<Long, Double> calculate(Long memberId, List<Hearit> hearits) {
        LocalDate now = LocalDate.now();
        return hearits.stream()
                .collect(Collectors.toMap(
                        Hearit::getId,
                        hearit -> {
                            LocalDate createdDate = hearit.getCreatedAt().toLocalDate();
                            long days = ChronoUnit.DAYS.between(createdDate, now);
                            double score = 20.0 - (days / 2.0);
                            return Math.max(0.0, score);
                        }
                ));
    }
}
