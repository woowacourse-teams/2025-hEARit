package com.onair.hearit.app.explore.application.scorefactor;

import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.PlayingHistory;
import com.onair.hearit.core.domain.UserType;
import com.onair.hearit.core.infrastructure.jpa.PlayingHistoryRepository;
import com.onair.hearit.core.infrastructure.projection.CategoryPlayingHistoryCount;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlayingHistoryScoreFactor implements ScoreFactor {

    private static final int RECENT_PLAYED_HEARIT_LIMIT = 60;

    private final PlayingHistoryRepository playingHistoryRepository;

    @Override
    public boolean isSupported(UserType userType) {
        return userType == UserType.GUEST || userType == UserType.MEMBER;
    }

    @Override
    public Map<Long, Double> calculate(UUID userUuid, List<Hearit> hearits) {
        List<Long> isFinishedHearitIds = getFinishedHearitIds(userUuid);
        Map<Long, Double> categoryScores = calculateCategoryPlayRate(userUuid);
        return hearits.stream()
                .collect(Collectors.toMap(
                        Hearit::getId,
                        hearit -> calculatePlayingHistoryScore(
                                isFinishedHearitIds.contains(hearit.getId()),
                                categoryScores.getOrDefault(hearit.getCategory().getId(), 0.0))
                ));
    }

    private Map<Long, Double> calculateCategoryPlayRate(UUID userUuid) {
        List<CategoryPlayingHistoryCount> playedCategoryCounts =
                playingHistoryRepository.countPlayingHistoriesByCategory(userUuid);
        if (playedCategoryCounts == null || playedCategoryCounts.isEmpty()) {
            return Map.of();
        }

        long maxCategoryPlayCount = playedCategoryCounts.stream()
                .mapToLong(CategoryPlayingHistoryCount::getCount)
                .max()
                .orElse(0L);

        return playedCategoryCounts.stream()
                .collect(Collectors.toMap(
                        CategoryPlayingHistoryCount::getCategoryId,
                        c -> (double) c.getCount() / (double) maxCategoryPlayCount));
    }

    private List<Long> getFinishedHearitIds(UUID userUuid) {
        List<PlayingHistory> playingHistories =
                playingHistoryRepository.findByUserUuidOrderByUpdatedAtDesc(userUuid, RECENT_PLAYED_HEARIT_LIMIT);
        return playingHistories.stream()
                .filter(PlayingHistory::isFinished)
                .map(PlayingHistory::getHearitId)
                .toList();
    }

    private double calculatePlayingHistoryScore(boolean isFinished, double categoryScore) {
        if (isFinished) {
            return 0.0;
        }
        return Math.clamp(categoryScore, 0, 1);
    }
}
