package com.onair.hearit.common.log;

import com.onair.hearit.common.log.message.JsonMaskingPrettyFormatter;
import com.onair.hearit.common.log.message.dto.ExceptionLog;
import com.onair.hearit.common.log.message.dto.ExceptionLog.ErrorDetail;
import com.onair.hearit.common.log.message.dto.RequestInfo;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FilterErrorLogger {

    private final JsonMaskingPrettyFormatter jsonMaskingPrettyFormatter;

    public void log(HttpServletRequest request, ProblemDetail problemDetail) {
        RequestInfo requestInfo = RequestInfo.from(request);
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
