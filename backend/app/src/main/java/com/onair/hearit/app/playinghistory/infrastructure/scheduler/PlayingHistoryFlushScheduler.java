package com.onair.hearit.app.playinghistory.infrastructure.scheduler;

import com.onair.hearit.app.playinghistory.infrastructure.buffer.PlayingHistoryBuffer;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlayingHistoryFlushScheduler {

    private final PlayingHistoryBuffer buffer;

    @Scheduled(fixedDelay = 1000)
    @SchedulerLock(
            name = "PlayingHistoryFlushScheduler",
            lockAtMostFor = "10s",
            lockAtLeastFor = "1s"
    )
    public void scheduleFlush() {
        try {
            buffer.flush();
        } catch (Exception e) {
            log.error("재생 기록 스케줄러 flush 실패", e);
        }
    }

    @PreDestroy
    public void shutdown() {
        log.info("애플리케이션 종료 시 재생 기록 flush 시작");
        try {
            buffer.flush();
        } catch (Exception e) {
            log.error("종료 시 flush 실패", e);
        }
    }
}
