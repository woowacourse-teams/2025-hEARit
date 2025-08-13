package com.onair.hearit.common.log.message;

import com.onair.hearit.common.log.message.dto.RequestInfo;
import com.onair.hearit.common.log.message.dto.RequestLog;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.logging.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger consoleLogger = LogManager.getLogger("consoleLogger");
    private static final Logger jsonLogger = LogManager.getLogger("jsonLogger");

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            filterChain.doFilter(request, response);
        } finally {
            Map<String, String[]> parameterMap = request.getParameterMap();

            RequestLog requestLog = RequestLog.ofFilter(
                    LocalDateTime.now(),
                    RequestInfo.fromMdc(),
                    parameterMap
            );

            boolean aopEntered = "true".equals(MDC.get("AOP_ENTERED"));
            if (!aopEntered) {
                jsonLogger.info(requestLog);
                consoleLogger.info(getRequestLogForConsole(requestLog));
            }
        }
    }

    private String getRequestLogForConsole(RequestLog log) {
        String method = log.getRequestInfo().getHttpMethod();
        String uri = log.getRequestInfo().getRequestUri();
        String ip = log.getRequestInfo().getIp();
        String time = log.getTimestamp();
        Map<String, List<String>> params = log.getRequestParameter();

        return String.format("[REQUEST] %s → %s %s from %s params=%s",
                time,
                method,
                uri,
                ip,
                toFlatParamString(params)
        );
    }

    private String toFlatParamString(Map<String, List<String>> params) {
        return params.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(", ", "{", "}"));
    }
}
