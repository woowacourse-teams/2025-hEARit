package com.onair.hearit.common.log;

import com.onair.hearit.common.log.mask.MaskingSupport;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class FilterExceptionLogger extends OncePerRequestFilter {

    private final Logger errorLogger = LogManager.getLogger("errorLogger");
    private static final Logger consoleLogger = LogManager.getLogger("consoleLogger");
    private static final Logger jsonLogger = LogManager.getLogger("jsonLogger");
    private final MaskingSupport maskingSupport;

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

            jsonLogger.error(maskingSupport.mask(exceptionLog));
            errorLogger.error(exceptionLog, ex);
            consoleLogger.error("[ERROR] {} {} from {} → {}",
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
                0);
        ExceptionLog exceptionLog = ExceptionLog.warn(
                LocalDateTime.now(),
                requestInfo,
                HttpStatus.resolve(problemDetail.getStatus()),
                errorDetail
        );

        jsonLogger.warn(maskingSupport.mask(exceptionLog));
        consoleLogger.warn("[CLIENT ERROR] {} {} from {} → status: {} / title: {} / detail: {}",
                requestInfo.getHttpMethod(),
                requestInfo.getRequestUri(),
                requestInfo.getIp(),
                problemDetail.getStatus(),
                problemDetail.getTitle(),
                problemDetail.getDetail()
        );
    }
}
