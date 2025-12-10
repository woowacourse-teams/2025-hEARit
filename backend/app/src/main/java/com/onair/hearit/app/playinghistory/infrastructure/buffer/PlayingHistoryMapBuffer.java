package com.onair.hearit.app.playinghistory.infrastructure.buffer;

import com.onair.hearit.app.exception.custom.BufferRequestException;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.PlayingHistory;
import com.onair.hearit.core.infrastructure.jdbc.PlayingHistoryCommandRepository;
import com.onair.hearit.core.infrastructure.jpa.HearitRepository;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PlayingHistoryMapBuffer implements PlayingHistoryBuffer {

    private static final int BUFFER_SIZE = 100_000;

    private final Map<PlayKey, PlayValue> cache = new ConcurrentHashMap<>();
    private final PlayingHistoryCommandRepository playingHistoryCommandRepository;
    private final HearitRepository hearitRepository;

    @Override
    public void add(PlayingHistory playingHistory, long clientEventTime) {
        PlayKey key = new PlayKey(playingHistory.getMemberId(), playingHistory.getHearitId());
        validateBufferSize(key);

        cache.compute(key, (k, existing) -> {
            PlayValue incoming = PlayValue.from(playingHistory, clientEventTime);
            if (existing != null && existing.isMoreRecentThan(incoming)) {
                return existing;
            }
            return incoming;
        });
    }

    private void validateBufferSize(PlayKey key) {
        if (!cache.containsKey(key) && cache.size() >= BUFFER_SIZE) {
            throw new BufferRequestException("버퍼 용량 초과로 인해 재생 기록 저장할 수 없습니다.");
        }
    }

    @Override
    @Transactional
    @Scheduled(fixedDelay = 1_000)
    public void flush() {
        Map<PlayKey, PlayValue> snapshot = createSnapshotAndRemoveFromCache();
        if (snapshot.isEmpty()) {
            return;
        }

        try {
            List<PlayingHistory> histories = buildHistoriesFromSnapshot(snapshot);
            playingHistoryCommandRepository.bulkInsert(histories);
        } catch (Exception e) {
            rollbackSnapshot(snapshot);
        }
    }

    private Map<PlayKey, PlayValue> createSnapshotAndRemoveFromCache() {
        Map<PlayKey, PlayValue> snapshot = Map.copyOf(cache);
        snapshot.keySet().forEach(cache::remove);
        return snapshot;
    }

    private List<PlayingHistory> buildHistoriesFromSnapshot(Map<PlayKey, PlayValue> snapshot) {
        Set<Long> hearitIds = snapshot.values().stream()
                .map(PlayValue::hearitId)
                .collect(Collectors.toSet());
        Map<Long, Hearit> hearitMap = hearitRepository.findAllById(hearitIds)
                .stream()
                .collect(Collectors.toMap(Hearit::getId, h -> h));
        return snapshot.values().stream().map(v -> new PlayingHistory(
                        v.memberId(),
                        hearitMap.get(v.hearitId()),
                        v.lastPlayTime()
                ))
                .toList();
    }

    private void rollbackSnapshot(Map<PlayKey, PlayValue> snapshot) {
        snapshot.forEach((key, newValue) ->
                cache.merge(key, newValue, (oldValue, incomingValue) -> {
                    if (oldValue.isMoreRecentThan(incomingValue)) {
                        return oldValue;
                    }
                    return incomingValue;
                })
        );
    }

    private record PlayKey(long memberId, long hearitId) {
    }

    private record PlayValue(long memberId, long hearitId, long lastPlayTime, long clientEventTime) {

        static PlayValue from(PlayingHistory history, long clientEventTime) {
            return new PlayValue(
                    history.getMemberId(),
                    history.getHearitId(),
                    history.getLastPlayTime(),
                    clientEventTime
            );
        }

        boolean isMoreRecentThan(PlayValue other) {
            return other != null && this.clientEventTime > other.clientEventTime();
        }
    }

    public Map<PlayKey, PlayValue> getCache() {
        return cache;
    }
}
