package com.onair.hearit.core.log.property.scheduler;

import com.onair.hearit.core.log.LogEvent;
import com.onair.hearit.core.log.property.LogProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BatchErrorLogProperty implements LogProperty {

    private final String jobName;
    private final String exceptionName;
    private final String errorMessage;

    public static BatchErrorLogProperty of(String jobName, Exception e) {
        return new BatchErrorLogProperty(jobName, e.getClass().getSimpleName(), e.getMessage());
    }

    @Override
    public String getEventName() {
        return LogEvent.BATCH_ERROR.getEventName();
    }
}
