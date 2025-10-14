package com.onair.hearit.core.log.property.api;

import com.onair.hearit.core.log.LogEvent;
import com.onair.hearit.core.log.property.LogProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ExceptionLogProperty implements LogProperty {

    private final String endPoint;
    private final String method;
    private final Status httpStatus;
    private final ErrorDetail errorDetail;

    public static ExceptionLogProperty errorWithoutThrowable(String endPoint, String method, HttpStatus httpStatus) {
        ErrorDetail errorDetail = ErrorDetail.emptyErrorDetail();
        return new ExceptionLogProperty(endPoint, method, Status.from(httpStatus), errorDetail);
    }

    public static ExceptionLogProperty warnFromProblemDetail(String endPoint, String method,
                                                             ProblemDetail problemDetail) {
        ErrorDetail errorDetail = ErrorDetail.fromProblemDetail(problemDetail);
        return new ExceptionLogProperty(endPoint, method, Status.from(HttpStatus.valueOf(problemDetail.getStatus())),
                errorDetail);
    }

    public static ExceptionLogProperty errorFromThrowable(String endPoint, String method, HttpStatus httpStatus,
                                                          Throwable throwable) {
        ErrorDetail errorDetail = ErrorDetail.fromThrowable(throwable);
        return new ExceptionLogProperty(endPoint, method, Status.from(httpStatus), errorDetail);
    }

    public static ExceptionLogProperty warnFromThrowable(String endPoint, String method, ProblemDetail problemDetail,
                                                         Throwable throwable) {
        ErrorDetail errorDetail = ErrorDetail.fromThrowable(throwable);
        return new ExceptionLogProperty(endPoint, method, Status.from(HttpStatus.valueOf(problemDetail.getStatus())),
                errorDetail);
    }

    public static ExceptionLogProperty from(String endPoint, String method, HttpStatus httpStatus,
                                            String exceptionMessage) {
        ErrorDetail errorDetail = ErrorDetail.of(
                exceptionMessage,
                "unknown",
                "unknown",
                -1,
                httpStatus.name());
        return new ExceptionLogProperty(endPoint, method, Status.from(httpStatus), errorDetail);
    }

    @Override
    public String getEventName() {
        return LogEvent.EXCEPTION.getEventName();
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
            return new ExceptionLogProperty.ErrorDetail("예외 정보가 없습니다.", null, null, -1, null);
        }

        public static ErrorDetail fromThrowable(Throwable throwable) {
            StackTraceElement[] stackTrace = throwable.getStackTrace();
            String exceptionName = throwable.getClass().getName();
            if (stackTrace.length == 0) {
                return new ExceptionLogProperty.ErrorDetail(
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

        public static ErrorDetail of(String message, String className, String methodName,
                                     int lineNumber, String exceptionName) {
            return new ErrorDetail(message, className, methodName, lineNumber, exceptionName);
        }

        public static ErrorDetail fromProblemDetail(ProblemDetail problemDetail) {
            return new ErrorDetail(
                    problemDetail.getDetail(),
                    problemDetail.getTitle(),
                    null,
                    0,
                    null);
        }
    }
}
