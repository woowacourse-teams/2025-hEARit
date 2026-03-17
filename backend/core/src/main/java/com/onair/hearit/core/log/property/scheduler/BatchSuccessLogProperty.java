package com.onair.hearit.core.log.property.scheduler;

import com.onair.hearit.core.log.LogEvent;
import com.onair.hearit.core.log.property.LogProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BatchSuccessLogProperty implements LogProperty {

    private final String jobName;
    private final long durationMs;

    public static BatchSuccessLogProperty of(String jobName, long durationMs) {
        return new BatchSuccessLogProperty(jobName, durationMs);
    }

    @Override
    public String getEventName() {
        return LogEvent.BATCH_SUCCESS.getEventName();
    }
}
