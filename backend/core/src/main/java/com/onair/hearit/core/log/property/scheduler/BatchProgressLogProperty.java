package com.onair.hearit.core.log.property.scheduler;

import com.onair.hearit.core.log.LogEvent;
import com.onair.hearit.core.log.property.LogProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BatchProgressLogProperty implements LogProperty {

    private final String jobName;
    private final String stepName; // ex: "Step 1: Feature Loading"

    public static BatchProgressLogProperty of(String jobName, String stepName) {
        return new BatchProgressLogProperty(jobName, stepName);
    }

    @Override
    public String getEventName() {
        return LogEvent.BATCH_PROGRESS.getEventName();
    }
}
