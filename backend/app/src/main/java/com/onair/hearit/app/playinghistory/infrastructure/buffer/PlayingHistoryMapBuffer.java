package com.onair.hearit.app.playinghistory.infrastructure.buffer;

import com.onair.hearit.app.exception.custom.BufferRequestException;
import com.onair.hearit.app.playinghistory.infrastructure.buffer.converter.PlayingHistoryConverter;
import com.onair.hearit.core.domain.PlayingHistory;
import com.onair.hearit.core.infrastructure.jdbc.PlayingHistoryCommandRepository;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlayingHistoryMapBuffer implements PlayingHistoryBuffer {

    private static final int BUFFER_SIZE = 100_000;

    private final Map<PlayKey, PlayValue> cache = new ConcurrentHashMap<>();
    private final PlayingHistoryCommandRepository playingHistoryCommandRepository;
    private final PlayingHistoryConverter converter;

    @Override
    public void add(PlayingHistory playingHistory, long clientEventTime) {
        PlayKey key = new PlayKey(playingHistory.getUserUuid(), playingHistory.getHearitId());
        cache.compute(key, (k, existing) -> {
            if (existing == null) {
                validateBufferSize(k);
            }
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
    public void flush() {
        Map<PlayKey, PlayValue> snapshot = createSnapshotAndRemoveFromCache();
        if (snapshot.isEmpty()) {
            return;
        }
        try {
            List<PlayingHistory> histories = converter.toPlayingHistories(snapshot.values());
            playingHistoryCommandRepository.bulkInsert(histories);
        } catch (Exception e) {
            rollbackSnapshot(snapshot);
            log.error("재생 기록 flush 실패, 롤백 수행. snapshot size: {}", snapshot.size(), e);
        }
    }

    @Override
    public int size() {
        return cache.size();
    }

    private Map<PlayKey, PlayValue> createSnapshotAndRemoveFromCache() {
        Map<PlayKey, PlayValue> snapshot = new ConcurrentHashMap<>();
        cache.forEach((key, value) -> {
            if (cache.remove(key, value)) {
                snapshot.put(key, value);
            }
        });
        return snapshot;
    }

    private void rollbackSnapshot(Map<PlayKey, PlayValue> snapshot) {
        try {
            snapshot.forEach((key, newValue) ->
                    cache.merge(key, newValue, (oldValue, incomingValue) -> {

                        if (oldValue.isMoreRecentThan(incomingValue)) {
                            return oldValue;
                        }
                        return incomingValue;
                    })
            );
        } catch (Exception e) {
            log.error("재생 기록 롤백 실패. snapshot size: {}", snapshot.size(), e);
        }
    }
}

