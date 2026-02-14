package com.onair.hearit.app.explore.application;

import com.onair.hearit.core.domain.UserType;
import com.onair.hearit.core.infrastructure.jdbc.ExploreScoreCommandRepository;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ExploreScoreInitializer {

    private final ExploreScoreCalculator exploreScoreCalculator;
    private final ExploreScoreCommandRepository exploreScoreCommandRepository;

    @Transactional
    public void refreshScores(long cursorId, UUID userUuid, UserType userType) {
        if (isInitialRequest(cursorId)) {
            upsertScores(userUuid, userType);
        }
    }

    @Transactional
    public void initializeScores(UUID userUuid, UserType userType) {
        upsertScores(userUuid, userType);
    }

    private void upsertScores(UUID userUuid, UserType userType) {
        Map<Long, Double> scores = exploreScoreCalculator.calculateTotalScores(userUuid, userType);
        exploreScoreCommandRepository.insertScores(userUuid, scores);
        exploreScoreCommandRepository.updateCursorIds(userUuid);
    }

    private boolean isInitialRequest(long cursorId) {
        return cursorId == 0L;
    }
}
