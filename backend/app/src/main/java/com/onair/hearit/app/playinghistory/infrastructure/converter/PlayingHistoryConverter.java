package com.onair.hearit.app.playinghistory.infrastructure.converter;

import com.onair.hearit.app.playinghistory.infrastructure.buffer.PlayHistoryValue;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.PlayingHistory;
import com.onair.hearit.core.infrastructure.jpa.HearitRepository;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlayingHistoryConverter {

    private final HearitRepository hearitRepository;

    public List<PlayingHistory> toPlayingHistories(Collection<PlayHistoryValue> playHistoryValues) {
        if (playHistoryValues.isEmpty()) {
            return List.of();
        }

        Set<Long> hearitIds = playHistoryValues.stream()
                .map(PlayHistoryValue::hearitId)
                .collect(Collectors.toSet());

        Map<Long, Hearit> hearitMap = hearitRepository.findAllById(hearitIds)
                .stream()
                .collect(Collectors.toMap(Hearit::getId, h -> h));

        return playHistoryValues.stream()
                .filter(playValue -> hearitMap.containsKey(playValue.hearitId()))
                .map(playValue -> new PlayingHistory(
                        playValue.userUuid(),
                        hearitMap.get(playValue.hearitId()),
                        playValue.lastPlayTime()
                ))
                .toList();
    }
}
