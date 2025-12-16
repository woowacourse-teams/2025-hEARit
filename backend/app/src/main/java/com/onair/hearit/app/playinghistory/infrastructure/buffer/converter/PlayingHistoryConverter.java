package com.onair.hearit.app.playinghistory.infrastructure.buffer.converter;

import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.PlayingHistory;
import com.onair.hearit.core.infrastructure.jpa.HearitRepository;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * PlayingHistory 변환 담당
 * - PlayValue → PlayingHistory 변환
 * - Hearit 엔티티 일괄 조회
 */
@Component
@RequiredArgsConstructor
public class PlayingHistoryConverter {

    private final HearitRepository hearitRepository;

    //PlayValue 컬렉션을 PlayingHistory 리스트로 변환
    public List<PlayingHistory> toPlayingHistories(Collection<PlayValue> playValues) {
        if (playValues.isEmpty()) {
            return List.of();
        }

        Set<Long> hearitIds = playValues.stream()
                .map(PlayValue::hearitId)
                .collect(Collectors.toSet());

        Map<Long, Hearit> hearitMap = hearitRepository.findAllById(hearitIds)
                .stream()
                .collect(Collectors.toMap(Hearit::getId, h -> h));

        return playValues.stream()
                .map(playValue -> new PlayingHistory(
                        playValue.userUuid(),
                        hearitMap.get(playValue.hearitId()),
                        playValue.lastPlayTime()
                ))
                .toList();
    }

    public record PlayValue(
            String userUuid,
            long hearitId,
            long lastPlayTime,
            long clientEventTime
    ) {
        public static PlayValue from(PlayingHistory history, long clientEventTime) {
            return new PlayValue(
                    history.getUserUuid(),
                    history.getHearitId(),
                    history.getLastPlayTime(),
                    clientEventTime
            );
        }

        public boolean isMoreRecentThan(PlayValue other) {
            return other != null && this.clientEventTime > other.clientEventTime();
        }
    }

    public record PlayKey(String userUuid, long hearitId) {
    }
}
