package com.onair.hearit.core.log.property.scheduler;

import com.onair.hearit.core.log.LogEvent;
import com.onair.hearit.core.log.property.LogProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BatchStartLogProperty implements LogProperty {

    private final String jobName;

    public static BatchStartLogProperty of(String jobName) {
        return new BatchStartLogProperty(jobName);
    }

    @Override
    public String getEventName() {
        return LogEvent.BATCH_START.getEventName();
    }
}
