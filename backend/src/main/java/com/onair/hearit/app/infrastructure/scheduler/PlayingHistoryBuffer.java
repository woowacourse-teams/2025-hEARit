package com.onair.hearit.app.infrastructure.scheduler;

import com.onair.hearit.common.domain.Hearit;
import com.onair.hearit.common.domain.PlayingHistory;
import com.onair.hearit.common.infrastructure.jdbc.PlayingHistoryCommandRepository;
import com.onair.hearit.common.infrastructure.jpa.HearitRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PlayingHistoryBuffer {

    private final PlayingHistoryCommandRepository playingHistoryCommandRepository;
    private final HearitRepository hearitRepository;

    private final BlockingQueue<HistoryRecord> queue = new LinkedBlockingQueue<>();

    public void addPlayingHistory(Long memberId, Long hearitId, Long lastPlayTime) {
        if (memberId == null || hearitId == null || lastPlayTime == null) {
            return;
        }
        queue.add(new HistoryRecord(memberId, hearitId, lastPlayTime));
    }

    /*
     * 5초마다 또는 500개 이상 모이면 DB에 flush()
     */
    @Transactional
    @Scheduled(fixedDelay = 5000)
    public void flush() {
        if (queue.isEmpty()) {
            return;
        }

        List<HistoryRecord> batchRecords = new ArrayList<>();
        queue.drainTo(batchRecords, 500);
        if (!batchRecords.isEmpty()) {
            List<Long> hearitIds = batchRecords.stream()
                    .map(HistoryRecord::hearitId)
                    .distinct()
                    .toList();
            Map<Long, Hearit> hearitMap = hearitRepository.findAllById(hearitIds).stream()
                    .collect(Collectors.toMap(Hearit::getId, hearit -> hearit));

            List<PlayingHistory> historiesToInsert = new ArrayList<>();
            for (HistoryRecord record : batchRecords) {
                Hearit hearit = hearitMap.get(record.hearitId());
                historiesToInsert.add(new PlayingHistory(record.memberId(), hearit, record.lastPlayTime()));
            }

            if (!historiesToInsert.isEmpty()) {
                playingHistoryCommandRepository.bulkInsert(historiesToInsert);
            }
        }
    }

    private record HistoryRecord(Long memberId, Long hearitId, Long lastPlayTime) {
    }
}
