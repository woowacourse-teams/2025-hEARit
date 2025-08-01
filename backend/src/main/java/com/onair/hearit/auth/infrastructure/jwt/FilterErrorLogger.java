package com.onair.hearit.auth.infrastructure.jwt;

import com.onair.hearit.common.log.message.LogMessageGenerator;
import com.onair.hearit.common.log.message.dto.ErrorLog;
import com.onair.hearit.common.log.message.dto.ErrorLog.ErrorDetail;
import com.onair.hearit.common.log.message.dto.RequestInfo;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FilterErrorLogger {

    private final LogMessageGenerator logMessageGenerator;
    private final Logger errorLogger = LogManager.getLogger("errorLogger");

    public void log(HttpServletRequest request, ProblemDetail problemDetail) {
        RequestInfo requestInfo = RequestInfo.from(request);

        ErrorDetail errorDetail = new ErrorDetail(problemDetail.getDetail(), null, null, -1);

        ErrorLog errorLog = ErrorLog.of(
                "Warn",
                LocalDateTime.now(),
                requestInfo,
                HttpStatus.resolve(problemDetail.getStatus()),
                errorDetail
        );

        errorLogger.warn(errorLog);
        log.warn(logMessageGenerator.convertToPrettyJson(errorLog));
    }
}