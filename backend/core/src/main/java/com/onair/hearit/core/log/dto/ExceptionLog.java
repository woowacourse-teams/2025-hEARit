package com.onair.hearit.core.log.dto;

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
    private final Status httpStatus;
    private final ErrorDetail errorDetail;

    public static ExceptionLog warn(
            LocalDateTime timestamp,
            RequestInfo requestInfo,
            HttpStatus httpStatus,
            ErrorDetail errorDetail) {
        return new ExceptionLog(LogLevel.WARN, timestamp.toString(), requestInfo, Status.from(httpStatus), errorDetail);
    }

    public static ExceptionLog error(
            LocalDateTime timestamp,
            RequestInfo requestInfo,
            HttpStatus httpStatus,
            ErrorDetail errorDetail) {
        return new ExceptionLog(LogLevel.ERROR, timestamp.toString(), requestInfo, Status.from(httpStatus),
                errorDetail);
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Status {
        private final int code;
        private final String name;

        public static Status from(HttpStatus status) {
            if (status == null) {
                return new Status(-1, "UNKNOWN");
            }
            return new Status(status.value(), status.name());
        }
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ErrorDetail {

        private final String message;
        private final String className;
        private final String methodName;
        private final int lineNumber;
        private final String exceptionName;

        public static ErrorDetail emptyErrorDetail() {
            return new ErrorDetail("예외 정보가 없습니다.", null, null, -1, null);
        }

        public static ErrorDetail fromThrowable(Throwable throwable) {
            StackTraceElement[] stackTrace = throwable.getStackTrace();
            String exceptionName = throwable.getClass().getName();
            if (stackTrace.length == 0) {
                return new ErrorDetail(
                        throwable.getMessage(),
                        throwable.getClass().getName(),
                        "unknown",
                        -1,
                        exceptionName
                );
            }
            StackTraceElement finalStackTraceElement = stackTrace[0];
            return new ErrorDetail(
                    throwable.getMessage(),
                    finalStackTraceElement.getClassName(),
                    finalStackTraceElement.getMethodName(),
                    finalStackTraceElement.getLineNumber(),
                    exceptionName
            );
        }

        public static ErrorDetail of(String message, String className, String methodName, int lineNumber, String exceptionName) {
            return new ErrorDetail(message, className, methodName, lineNumber, exceptionName);
        }
    }
}
