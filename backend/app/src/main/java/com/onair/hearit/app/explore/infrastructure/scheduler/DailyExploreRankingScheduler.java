package com.onair.hearit.app.explore.infrastructure.scheduler;

import com.onair.hearit.app.cluster.application.HearitClusterBatchService;
import com.onair.hearit.app.explore.application.ExploreRankingBatchService;
import com.onair.hearit.core.log.logger.JsonLogger;
import com.onair.hearit.core.log.property.scheduler.BatchErrorLogProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DailyExploreRankingScheduler {

    private static final String JOB_NAME = "BATCH_SCHEDULER";

    private final JsonLogger jsonLogger;
    private final HearitClusterBatchService hearitClusterBatchService;
    private final ExploreRankingBatchService exploreRankingBatchService;

    /**
     * 매일 새벽 4시에 전체 배치 프로세스 실행
     * 1. 콘텐츠 군집화 (Clustering)
     * 2. 유저별 랭킹 산정 (Ranking)
     */
//    @Scheduled(cron = "0 0 4 * * *")
    @Scheduled(cron = "0 * * * * *")
    public void runExploreRankingJob() {
        try {
            hearitClusterBatchService.runClustering();
            exploreRankingBatchService.runRankingForExplore();
        } catch (Exception e) {
            jsonLogger.error(BatchErrorLogProperty.of(JOB_NAME, e), e);
        }
    }
}
