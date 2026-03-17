package com.onair.hearit.core.infrastructure.projection;

import java.time.LocalDateTime;

public interface HearitClusterStatisticsProjection {

    Long getHearitId();

    long getViewCount();

    long getLikeCount();

    long getBookmarkCount();

    double getAvgPlayTime();

    double getCompletionRate();

    LocalDateTime getCreatedAt();
}
