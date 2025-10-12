package com.onair.hearit.core.log.exception;

import com.onair.hearit.core.log.dto.ExceptionLog;
import com.onair.hearit.core.log.dto.ExceptionLog.ErrorDetail;
import com.onair.hearit.core.log.dto.RequestInfo;
import com.onair.hearit.core.log.logger.ConsoleLogger;
import com.onair.hearit.core.log.logger.JsonLogger;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class FilterExceptionLogger extends OncePerRequestFilter {

    private final ConsoleLogger consoleLogger;
    private final JsonLogger jsonLogger;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (RuntimeException ex) {
            RequestInfo requestInfo = RequestInfo.fromMdc();
            ErrorDetail errorDetail = ErrorDetail.fromThrowable(ex);
            ExceptionLog exceptionLog = ExceptionLog.error(
                    LocalDateTime.now(),
                    requestInfo,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    errorDetail);

            jsonLogger.error(exceptionLog);
            consoleLogger.error("[FILTER ERROR] {} {} from {} → {}",
                    requestInfo.getHttpMethod(),
                    requestInfo.getRequestUri(),
                    requestInfo.getIp(),
                    ex.toString(),
                    ex);

            throw ex;
        }
    }

    public void warn(ProblemDetail problemDetail) {
        RequestInfo requestInfo = RequestInfo.fromMdc();
        ErrorDetail errorDetail = ErrorDetail.of(
                problemDetail.getDetail(),
                problemDetail.getTitle(),
                null,
                0,
                null);
        ExceptionLog exceptionLog = ExceptionLog.warn(
                LocalDateTime.now(),
                requestInfo,
                HttpStatus.resolve(problemDetail.getStatus()),
                errorDetail
        );

        jsonLogger.warn(exceptionLog);
        consoleLogger.warn("[FILTER WARN] {} {} from {} → status: {} / title: {} / detail: {}",
                requestInfo.getHttpMethod(),
                requestInfo.getRequestUri(),
                requestInfo.getIp(),
                problemDetail.getStatus(),
                problemDetail.getTitle(),
                problemDetail.getDetail()
        );
    }
}
