package com.onair.hearit.app.infrastructure.scheduler;

import com.onair.hearit.common.domain.PlayingHistory;
import com.onair.hearit.common.exception.custom.BufferRequestException;
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

    private static final int BUFFER_SIZE = 10_000;

    private final PlayingHistoryCommandRepository playingHistoryCommandRepository;
    private final BlockingQueue<PlayingHistory> queue = new LinkedBlockingQueue<>(BUFFER_SIZE);

    public void add(PlayingHistory playingHistory) {
        if (!queue.offer(playingHistory)) {
            throw new BufferRequestException("큐가 가득 차서 요청을 처리할 수 없습니다.");
        }
    }

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
