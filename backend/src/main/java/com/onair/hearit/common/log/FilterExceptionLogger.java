package com.onair.hearit.common.log;

import com.onair.hearit.common.log.message.JsonMaskingPrettyFormatter;
import com.onair.hearit.common.log.message.dto.ExceptionLog;
import com.onair.hearit.common.log.message.dto.ExceptionLog.ErrorDetail;
import com.onair.hearit.common.log.message.dto.RequestInfo;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class FilterExceptionLogger extends OncePerRequestFilter {

    private final JsonMaskingPrettyFormatter jsonMaskingPrettyFormatter;
    private final Logger errorLogger = LogManager.getLogger("errorLogger");

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

            log.error(jsonMaskingPrettyFormatter.convertToPrettyJson(exceptionLog));
            errorLogger.error(exceptionLog, ex);

            throw ex;
        }
    }

    public void warn(ProblemDetail problemDetail) {
        RequestInfo requestInfo = RequestInfo.fromMdc();
        ErrorDetail errorDetail = ErrorDetail.of(
                problemDetail.getDetail(),
                problemDetail.getTitle(),
                null,
                0);
        ExceptionLog exceptionLog = ExceptionLog.warn(
                LocalDateTime.now(),
                requestInfo,
                HttpStatus.resolve(problemDetail.getStatus()),
                errorDetail
        );

        log.warn(jsonMaskingPrettyFormatter.convertToPrettyJson(exceptionLog));
    }
}
