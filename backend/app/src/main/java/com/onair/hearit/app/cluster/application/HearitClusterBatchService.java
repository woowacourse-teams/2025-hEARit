package com.onair.hearit.app.cluster.application;

import com.onair.hearit.core.infrastructure.jdbc.HearitClusterCommandRepository;
import com.onair.hearit.core.log.logger.JsonLogger;
import com.onair.hearit.core.log.property.scheduler.BatchErrorLogProperty;
import com.onair.hearit.core.log.property.scheduler.BatchProgressLogProperty;
import com.onair.hearit.core.log.property.scheduler.BatchStartLogProperty;
import com.onair.hearit.core.log.property.scheduler.BatchSuccessLogProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HearitClusterBatchService {

    private static final String JOB_NAME = "HEARIT_CLUSTER_BATCH";
    private static final int STATISTICS_CHUNK_SIZE = 100;
    private static final int CLUSTER_K_SIZE = 5;

    private final JsonLogger jsonLogger;
    private final HearitClusterCommandRepository hearitClusterCommandRepository;
    private final HearitClusterFeatureLoader hearitClusterFeatureLoader;
    private final HearitClusterCalculator hearitClusterCalculator;

    public void runClustering() {
        jsonLogger.info(BatchStartLogProperty.of(JOB_NAME));
        long startTime = System.currentTimeMillis();

        try {
            jsonLogger.info(BatchProgressLogProperty.of(JOB_NAME, "Step 0: Cleaning up Orphan Records"));
            hearitClusterCommandRepository.deleteOrphanClusters();

            jsonLogger.info(BatchProgressLogProperty.of(JOB_NAME, "Step 1: Loading Statistics Features"));
            hearitClusterFeatureLoader.loadStatisticsFeature(STATISTICS_CHUNK_SIZE);

            jsonLogger.info(BatchProgressLogProperty.of(JOB_NAME, "Step 2: Calculating K-Means Clusters"));
            hearitClusterCalculator.calculateClusters(CLUSTER_K_SIZE);

            long duration = System.currentTimeMillis() - startTime;
            jsonLogger.info(BatchSuccessLogProperty.of(JOB_NAME, duration));

        } catch (Exception e) {
            jsonLogger.error(BatchErrorLogProperty.of(JOB_NAME, e), e);
            throw e;
        }
    }
}
