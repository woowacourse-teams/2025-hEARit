package com.onair.hearit.app.explore.application;

import com.onair.hearit.core.domain.UserType;
import com.onair.hearit.core.infrastructure.jpa.MemberRepository;
import com.onair.hearit.core.infrastructure.jpa.PlayingHistoryRepository;
import com.onair.hearit.core.infrastructure.jpa.RefreshTokenRepository;
import com.onair.hearit.core.log.logger.JsonLogger;
import com.onair.hearit.core.log.property.scheduler.BatchErrorLogProperty;
import com.onair.hearit.core.log.property.scheduler.BatchProgressLogProperty;
import com.onair.hearit.core.log.property.scheduler.BatchStartLogProperty;
import com.onair.hearit.core.log.property.scheduler.BatchSuccessLogProperty;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExploreRankingBatchService {

    private static final String JOB_NAME = "EXPLORE_RANKING_BATCH";
    private static final int MAX_ACTIVE_GUEST_DAYS = 7;

    private final JsonLogger jsonLogger;
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PlayingHistoryRepository playingHistoryRepository;
    private final ExploreScoreInitializer exploreScoreInitializer;

    public void runRankingForExplore() {
        jsonLogger.info(BatchStartLogProperty.of(JOB_NAME));
        long startTime = System.currentTimeMillis();

        try {
            jsonLogger.info(BatchProgressLogProperty.of(JOB_NAME, "Step 1: Fetch target UUIDs about active users"));
            Set<UUID> targetUuids = fetchActiveUserUuids();
            int targetSize = targetUuids.size();

            jsonLogger.info(BatchProgressLogProperty.of(JOB_NAME, "Step 2: Initialize " + targetSize + " users"));
            for (UUID uuid : targetUuids) {
                UserType userType = determineUserType(uuid);
                exploreScoreInitializer.initializeScores(uuid, userType);
            }

            long duration = System.currentTimeMillis() - startTime;
            jsonLogger.info(BatchSuccessLogProperty.of(JOB_NAME, duration));

        } catch (Exception e) {
            jsonLogger.error(BatchErrorLogProperty.of(JOB_NAME, e), e);
            throw e;
        }
    }

    private Set<UUID> fetchActiveUserUuids() {
        Set<UUID> activeUserUuids = new HashSet<>();
        LocalDateTime now = LocalDateTime.now();

        activeUserUuids.addAll(refreshTokenRepository.findActiveMemberUuids(now));
        activeUserUuids.addAll(playingHistoryRepository.findUuidsByUpdatedAtAfter(now.minusDays(MAX_ACTIVE_GUEST_DAYS)));
        return activeUserUuids;
    }

    private UserType determineUserType(UUID uuid) {
        if (memberRepository.findByUuid(uuid).isPresent()) {
            return UserType.MEMBER;
        }
        return UserType.GUEST;
    }
}
