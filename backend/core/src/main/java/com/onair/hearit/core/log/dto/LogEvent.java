package com.onair.hearit.core.log.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LogEvent {

    REQUEST("apiRequest"),
    RESPONSE("apiResponse"),
    EXCEPTION("exception"),
    ;

    private final String eventName;
}
