package com.onair.hearit.app.cluster.infrastructure.scheduler;

import com.onair.hearit.app.cluster.application.HearitClusterBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HearitClusterScheduler {

    private final HearitClusterBatchService clusterBatchService;

    /**
     * 매일 새벽 4시에 콘텐츠(Hearit)에 대한 군집화(Clustering) 실행
     */
    @Scheduled(cron = "0 0 4 * * *")
    public void runClusteringJob() {
        clusterBatchService.runClustering();
    }
}
