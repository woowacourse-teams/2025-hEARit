package com.onair.hearit.common.log.message;

import com.onair.hearit.common.log.message.dto.RequestInfo;
import com.onair.hearit.common.log.message.dto.RequestLog;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jboss.logging.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Log4j2
@Component
@RequiredArgsConstructor
public class RequestLoggingFilter extends OncePerRequestFilter {

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
                log.info(requestLog);
            }
        }
    }
}
