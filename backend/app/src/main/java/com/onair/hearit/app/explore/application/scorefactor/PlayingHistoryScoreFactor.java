package com.onair.hearit.app.explore.application.scorefactor;

import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.PlayingHistory;
import com.onair.hearit.core.domain.UserType;
import com.onair.hearit.core.infrastructure.jpa.PlayingHistoryRepository;
import com.onair.hearit.core.infrastructure.projection.CategoryPlayingHistoryCount;
import java.util.List;
import java.util.Map;
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
        return userType == UserType.MEMBER;
    }

    @Override
    public Map<Long, Double> calculate(String userUuid, List<Hearit> hearits) {
        List<Long> isFinishedHearitIds = getFinishedHearitIds(userUuid);
        Map<Long, Double> categoryScores = calculateCategoryScores(userUuid);
        return hearits.stream()
                .collect(Collectors.toMap(
                        Hearit::getId,
                        hearit -> calculatePlayingHistoryScore(
                                isFinishedHearitIds.contains(hearit.getId()),
                                categoryScores.getOrDefault(hearit.getCategory().getId(), 0.0))
                ));
    }

    private Map<Long, Double> calculateCategoryScores(String userUuid) {
        List<CategoryPlayingHistoryCount> playedCategoryCounts =
                playingHistoryRepository.countPlayingHistoriesByCategory(userUuid);
        if (playedCategoryCounts == null || playedCategoryCounts.isEmpty()) {
            return Map.of();
        }

        long maxCount = playedCategoryCounts.stream()
                .mapToLong(c -> c.getCount() == null ? 0L : c.getCount())
                .max()
                .orElse(0L);

        return playedCategoryCounts.stream()
                .collect(Collectors.toMap(
                        CategoryPlayingHistoryCount::getCategoryId,
                        c -> {
                            long count = c.getCount() == null ? 0L : c.getCount();
                            return (double) count / (double) maxCount;
                        }
                ));
    }

    private List<Long> getFinishedHearitIds(String userUuid) {
        List<PlayingHistory> playingHistories =
                playingHistoryRepository.findByUserUuidOrderByUpdatedAtDesc(userUuid, RECENT_PLAYED_HEARIT_LIMIT);
        return playingHistories.stream()
                .filter(PlayingHistory::isFinished)
                .map(PlayingHistory::getHearitId)
                .toList();
    }

    private Double calculatePlayingHistoryScore(boolean isFinished, double categoryScore) {
        if (isFinished) {
            return 0.0;
        }
        return Math.clamp(categoryScore, 0, 1);
    }
}
