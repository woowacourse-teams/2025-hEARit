package com.onair.hearit.common.log.message.dto;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.logging.LogLevel;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ExceptionLog {

    private final LogLevel logType;
    private final String timestamp;
    private final RequestInfo requestInfo;
    private final HttpStatus httpStatus;
    private final ErrorDetail errorDetail;

    public static ExceptionLog warn(
            LocalDateTime timestamp,
            RequestInfo requestInfo,
            HttpStatus httpStatus,
            ErrorDetail errorDetail) {
        return new ExceptionLog(LogLevel.WARN, timestamp.toString(), requestInfo, httpStatus, errorDetail);
    }

    public static ExceptionLog error(
            LocalDateTime timestamp,
            RequestInfo requestInfo,
            HttpStatus httpStatus,
            ErrorDetail errorDetail) {
        return new ExceptionLog(LogLevel.ERROR, timestamp.toString(), requestInfo, httpStatus, errorDetail);
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ErrorDetail {

        private final String message;
        private final String className;
        private final String methodName;
        private final int lineNumber;

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

        public static ErrorDetail of(String message, String className, String methodName, int lineNumber) {
            return new ErrorDetail(message, className, methodName, lineNumber);
        }
    }
}
