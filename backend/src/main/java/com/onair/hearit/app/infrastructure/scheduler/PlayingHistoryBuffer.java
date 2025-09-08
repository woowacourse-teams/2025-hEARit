package com.onair.hearit.app.infrastructure.scheduler;

import com.onair.hearit.common.domain.PlayingHistory;
import com.onair.hearit.common.infrastructure.jdbc.PlayingHistoryCommandRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PlayingHistoryBuffer {

    private final PlayingHistoryCommandRepository playingHistoryCommandRepository;
    private final BlockingQueue<PlayingHistory> queue = new LinkedBlockingQueue<>();

    public void add(PlayingHistory playingHistory) {
        if (playingHistory != null) {
            queue.add(playingHistory);
        }
    }

    /*
     * 5초마다 또는 500개 이상 모이면 DB에 flush()
     */
    @Transactional
    @Scheduled(fixedDelay = 5000)
    public void flush() {
        if (!queue.isEmpty()) {
            List<PlayingHistory> batchRecords = new ArrayList<>();
            queue.drainTo(batchRecords, 500);
            if (!batchRecords.isEmpty()) {
                playingHistoryCommandRepository.bulkInsert(batchRecords);
            }
        }
    }
}
