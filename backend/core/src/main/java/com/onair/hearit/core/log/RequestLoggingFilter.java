package com.onair.hearit.core.log;

import com.onair.hearit.core.log.dto.logproperty.RequestLogProperty;
import com.onair.hearit.core.log.logger.ConsoleLogger;
import com.onair.hearit.core.log.logger.JsonLogger;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final List<String> excludedPaths = List.of(
            "/admin/**",
            "/api/v1/admin/**",
            "/favicon.ico",
            "/.well-known/**",
            "/actuator/**"
    );

    private final JsonLogger jsonLogger;
    private final ConsoleLogger consoleLogger;
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
            RequestLogProperty requestLogProperty = RequestLogProperty.forFilter(request);

            boolean aopEntered = "true".equals(MDC.get("AOP_ENTERED"));
            if (!aopEntered) {
                jsonLogger.info(requestLogProperty);
                consoleLogger.info(requestLogProperty);
            }
        }
    }

    private boolean isExcludedPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        return excludedPaths.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }
}
