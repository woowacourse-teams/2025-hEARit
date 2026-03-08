package com.onair.hearit.app.explore.infrastructure.scheduler;

import com.onair.hearit.app.explore.application.ExploreRankingFacade;
import com.onair.hearit.core.log.logger.JsonLogger;
import com.onair.hearit.core.log.property.scheduler.BatchErrorLogProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyExploreRankingScheduler {

    private static final String JOB_NAME = "BATCH_SCHEDULER";

    private final JsonLogger jsonLogger;
    private final ExploreRankingFacade exploreRankingFacade;

    /**
     * 매일 새벽 4시에 전체 배치 프로세스 실행
     */
    @Scheduled(cron = "0 0 4 * * *")
    public void runExploreRankingJob() {
        try {
            boolean executed = exploreRankingFacade.runWithLock();
            if (!executed) {
                log.info("Scheduled batch skipped: already running");
            }
        } catch (Exception e) {
            jsonLogger.error(BatchErrorLogProperty.of(JOB_NAME, e), e);
        }
    }
}
