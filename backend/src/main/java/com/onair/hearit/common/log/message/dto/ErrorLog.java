package com.onair.hearit.common.log.message.dto;

import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;

public record ErrorLog(
        String logType,
        String timestamp,
        RequestInfo requestInfo,
        HttpStatus httpStatus,
        ErrorDetail errorDetail
) {

    public static ErrorLog of(
            String logType,
            LocalDateTime timestamp,
            RequestInfo requestInfo,
            HttpStatus httpStatus,
            ErrorDetail errorDetail) {
        return new ErrorLog(logType, timestamp.toString(), requestInfo, httpStatus, errorDetail);
    }

    public record ErrorDetail(
            String message,
            String className,
            String methodName,
            int lineNumber
    ) {

        public static ErrorDetail emptyErrorDetail() {
            return new ErrorDetail("예외 정보가 없습니다.", null, null, -1);
        }

        public static ErrorDetail fromThrowable(Throwable throwable) {
            StackTraceElement[] stackTrace = throwable.getStackTrace();
            if (stackTrace.length == 0) {
                return new ErrorDetail(
                        throwable.getMessage(),
                        throwable.getClass().getName(),
                        "unknown",
                        -1
                );
            }
            StackTraceElement finalStackTraceElement = stackTrace[0];
            return new ErrorDetail(
                    throwable.getMessage(),
                    finalStackTraceElement.getClassName(),
                    finalStackTraceElement.getMethodName(),
                    finalStackTraceElement.getLineNumber()
            );
        }
    }
}
