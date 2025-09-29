package com.onair.hearit.explore.application;

import com.onair.hearit.domain.UserType;
import com.onair.hearit.infrastructure.jdbc.ExploreScoreCommandRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ExploreScoreRefresher {

    private final ExploreScoreCalculator exploreScoreCalculator;
    private final ExploreScoreCommandRepository exploreScoreCommandRepository;

    @Transactional
    public void refreshIfNeeded(long cursorId, String userUuid, UserType userType) {
        if (cursorId != 0L) {
            return;
        }
        Map<Long, Double> scores = exploreScoreCalculator.calculateTotalScores(userUuid, userType);
        exploreScoreCommandRepository.insertScores(userUuid, scores);
        exploreScoreCommandRepository.updateCursorIds(userUuid);
    }
}

