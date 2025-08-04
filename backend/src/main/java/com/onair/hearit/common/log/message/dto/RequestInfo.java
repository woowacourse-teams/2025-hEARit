package com.onair.hearit.common.log.message.dto;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
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

    public static RequestInfo from(HttpServletRequest request) {
        String id = UUID.randomUUID().toString();
        String ip = request.getRemoteAddr();
        String httpMethod = request.getMethod();
        String requestUri = request.getRequestURI();
        return new RequestInfo(id, ip, httpMethod, requestUri);
    }

    public static RequestInfo getCurrentRequestInfo() {
        return new RequestInfo(
                MDC.get("id"),
                MDC.get("ip"),
                MDC.get("httpMethod"),
                MDC.get("requestUri"));
    }
}
