package com.onair.hearit.app.playinghistory.infrastructure.buffer;

import com.onair.hearit.core.domain.PlayingHistory;
import com.onair.hearit.core.infrastructure.jdbc.PlayingHistoryCommandRepository;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;

//@Component
@RequiredArgsConstructor
public class PlayingHistoryMapBuffer implements PlayingHistoryBuffer {

    private static final int BUFFER_SIZE = 100_000;

    private final PlayingHistoryCommandRepository playingHistoryCommandRepository;

    private final Map<PlayKey, PlayValue> cache = new ConcurrentHashMap<>();

    @Override
    public void add(PlayingHistory playingHistory, long clientEventTime) {

    }

    @Override
    public void flush() {

    }

    private record PlayKey(long memberId, long hearitId) {

    }

    private record PlayValue(long memberId, long hearitId, long lastPlayTime, long clientEventTime) {

    }
}
