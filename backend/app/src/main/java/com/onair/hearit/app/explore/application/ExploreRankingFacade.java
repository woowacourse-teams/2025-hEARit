package com.onair.hearit.app.explore.application;

import com.onair.hearit.app.cluster.application.HearitClusterBatchService;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockingTaskExecutor;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExploreRankingFacade {

    public static final String LOCK_NAME = "runExploreRankingJobLock";

    private final HearitClusterBatchService hearitClusterBatchService;
    private final ExploreRankingBatchService exploreRankingBatchService;
    private final LockingTaskExecutor lockExecutor;

    public boolean runWithLock() {
        log.info("Attempting to acquire lock for Explore Ranking Process...");
        try {
            LockingTaskExecutor.TaskResult<Void> result = lockExecutor.executeWithLock(
                    () -> {
                        runExploreRankingProcess();
                        return null;
                    },
                    new LockConfiguration(
                            Instant.now(),
                            LOCK_NAME,
                            Duration.ofMinutes(30),
                            Duration.ofMinutes(1)
                    )
            );
            return result.wasExecuted();
        } catch (Throwable e) {
            log.error("Error occurred during locked execution of Explore Ranking Process", e);
            return false;
        }
    }

    private void runExploreRankingProcess() {
        hearitClusterBatchService.runClustering();
        exploreRankingBatchService.runRankingForExplore();
    }
}
