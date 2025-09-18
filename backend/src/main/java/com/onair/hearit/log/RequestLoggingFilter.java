package com.onair.hearit.log;

import com.onair.hearit.log.dto.RequestInfo;
import com.onair.hearit.log.dto.RequestLog;
import com.onair.hearit.log.formatter.ConsoleLogFormatter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger consoleLogger = LogManager.getLogger("consoleLogger");
    private static final Logger jsonLogger = LogManager.getLogger("jsonLogger");

    private static final List<String> excludedPaths = List.of(
            "/admin/**",
            "/api/v1/admin/**",
            "/favicon.ico",
            "/.well-known/**",
            "/actuator/**"
    );

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        if (isExcludedPath(request)) {
            filterChain.doFilter(request, response);
            return;
        }

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
                consoleLogger.info(ConsoleLogFormatter.formatRequestLog(requestLog));
            }
        }
    }

    private boolean isExcludedPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        return excludedPaths.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }
}
