package com.onair.hearit.core.log.dto.logproperty.db;

import com.onair.hearit.core.log.dto.LogEvent;
import com.onair.hearit.core.log.dto.logproperty.LogProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DbErrorLogProperty implements LogProperty {

    private final String query;
    private final String targetMethod;
    private final String exceptionName;
    private final String exceptionMessage;

    public static DbErrorLogProperty of(String query, String targetMethod, String exceptionName,
                                        String exceptionMessage) {
        return new DbErrorLogProperty(query, targetMethod, exceptionName, exceptionMessage);
    }

    @Override
    public String getEventName() {
        return LogEvent.DB_ERROR.getEventName();
    }
}
