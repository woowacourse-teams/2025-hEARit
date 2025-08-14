package com.onair.hearit.common.log.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.slf4j.MDC;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestInfo {

    private final String id;
    private final String ip;
    private final String httpMethod;
    private final String requestUri;

    public static RequestInfo fromMdc() {
        return new RequestInfo(
                MDC.get("id"),
                MDC.get("ip"),
                MDC.get("httpMethod"),
                MDC.get("requestUri"));
    }
}
