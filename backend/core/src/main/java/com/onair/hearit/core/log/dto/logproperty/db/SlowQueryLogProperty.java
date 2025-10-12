package com.onair.hearit.core.log.dto.logproperty.db;

import com.onair.hearit.core.log.dto.LogEvent;
import com.onair.hearit.core.log.dto.logproperty.LogProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SlowQueryLogProperty implements LogProperty {

    private final String query;
    private final long executionTimeMs;
    private final String targetMethod;

    public static SlowQueryLogProperty of(String query, long executionTime, String methodName) {
        return new SlowQueryLogProperty(query, executionTime, methodName);
    }

    @Override
    public String getEventName() {
        return LogEvent.DB_SLOW_QUERY.getEventName();
    }
}
