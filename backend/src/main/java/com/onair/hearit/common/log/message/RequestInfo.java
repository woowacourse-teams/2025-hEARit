package com.onair.hearit.common.log.message;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.slf4j.MDC;

public record RequestInfo(
        String id,
        String ip,
        String httpMethod,
        String requestUri
) {

    public static RequestInfo from(HttpServletRequest request) {
        String id = UUID.randomUUID().toString();
        String ip = request.getRemoteAddr();
        String httpMethod = request.getMethod();
        String requestUri = request.getRequestURI();
        return new RequestInfo(id, ip, httpMethod, requestUri);
    }

    public static RequestInfo getCurrentHttpRequest() {
        return new RequestInfo(
                MDC.get("id"),
                MDC.get("ip"),
                MDC.get("httpMethod"),
                MDC.get("requestUri"));
    }
}
