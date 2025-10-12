package com.onair.hearit.core.log.dto;

import lombok.Getter;

@Getter
public class LogFormat {

    private final String eventName;
    private final LogProperty properties;

    public LogFormat(LogProperty logProperty) {
        this.properties = logProperty;
        this.eventName = logProperty.getlogEvent().getEventName();
    }
}
