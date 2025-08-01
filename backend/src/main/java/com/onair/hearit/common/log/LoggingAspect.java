package com.onair.hearit.common.log;

import com.onair.hearit.common.log.message.LogMessageGenerator;
import com.onair.hearit.common.log.message.dto.ErrorLog;
import com.onair.hearit.common.log.message.dto.ErrorLog.ErrorDetail;
import com.onair.hearit.common.log.message.dto.RequestInfo;
import com.onair.hearit.common.log.message.dto.RequestLog;
import com.onair.hearit.common.log.message.dto.ResponseLog;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Slf4j
@Component
@RequiredArgsConstructor
public class LoggingAspect {

    private static final String START_TIME_KEY = "startTime";

    private final LogMessageGenerator logMessageGenerator;
    private final Logger errorLogger = LogManager.getLogger("errorLogger");

    @Pointcut("@annotation(org.springframework.web.bind.annotation.GetMapping)")
    public void getMapping() {
    }

    @Pointcut("@annotation(org.springframework.web.bind.annotation.PostMapping)")
    public void postMapping() {
    }

    @Pointcut("@annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    public void deleteMapping() {
    }

    @Pointcut("@annotation(org.springframework.web.bind.annotation.PutMapping)")
    public void putMapping() {
    }

    @Pointcut("@annotation(org.springframework.web.bind.annotation.PatchMapping)")
    public void patchMapping() {
    }

    @Pointcut("getMapping() || postMapping() || deleteMapping() || putMapping() || patchMapping()")
    public void allMapping() {
    }

    @Pointcut("within(@org.springframework.web.bind.annotation.RestControllerAdvice *) || "
            + "within(@org.springframework.web.bind.annotation.ControllerAdvice *)")
    public void exceptionHandler() {
    }

    @Before("allMapping()")
    public void logRequest(JoinPoint joinPoint) {
        MDC.put(START_TIME_KEY, String.valueOf(System.currentTimeMillis()));
        RequestLog requestLog = getRequestLog(joinPoint);
        log.info(logMessageGenerator.convertToPrettyJson(requestLog));
    }

    private RequestLog getRequestLog(JoinPoint joinPoint) {
        HttpServletRequest request = getHttpServletRequest();
        RequestInfo requestInfo = RequestInfo.from(request);
        putRequestInfoToMdc(requestInfo);
        return RequestLog.of(
                LocalDateTime.now(),
                requestInfo,
                request.getParameterMap(),
                extractRequestBody(joinPoint));
    }

    private HttpServletRequest getHttpServletRequest() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(ServletRequestAttributes.class::isInstance)
                .map(ServletRequestAttributes.class::cast)
                .map(ServletRequestAttributes::getRequest)
                .orElseThrow(() -> new IllegalStateException("현재 스레드에 바인딩 된 request가 없습니다."));
    }

    private void putRequestInfoToMdc(RequestInfo requestInfo) {
        MDC.put("id", requestInfo.id());
        MDC.put("ip", requestInfo.ip());
        MDC.put("httpMethod", requestInfo.httpMethod());
        MDC.put("requestUri", requestInfo.requestUri());
    }

    private Object extractRequestBody(JoinPoint joinPoint) {
        return Arrays.stream(joinPoint.getArgs())
                .filter(Objects::nonNull)
                .filter(arg -> !(arg instanceof HttpServletRequest) && !(arg instanceof HttpServletResponse))
                .findFirst()
                .orElse(null);
    }

    @AfterReturning(value = "allMapping()", returning = "responseEntity")
    public void logResponse(ResponseEntity<?> responseEntity) {
        try {
            RequestInfo requestInfo = RequestInfo.getCurrentHttpRequest();
            ResponseLog responseLog = ResponseLog.of(
                    LocalDateTime.now(),
                    requestInfo,
                    responseEntity,
                    calculateTimeTakenMs());
            log.info(logMessageGenerator.convertToPrettyJson(responseLog));
        } finally {
            MDC.clear();
        }
    }

    private long calculateTimeTakenMs() {
        return Optional.ofNullable(MDC.get(START_TIME_KEY))
                .map(Long::parseLong)
                .map(startTime -> System.currentTimeMillis() - startTime)
                .orElseGet(() -> {
                    log.warn("startTime이 MDC에 존재하지 않습니다. 처리 시간을 계산할 수 없습니다.");
                    return -1L;
                });
    }

    @AfterReturning(value = "exceptionHandler()", returning = "problemDetail")
    public void logExceptionHandler(JoinPoint joinPoint, ProblemDetail problemDetail) {
        try {
            RequestInfo requestInfo = RequestInfo.getCurrentHttpRequest();
            Optional<Throwable> throwable = extractThrowableFromArgs(joinPoint.getArgs());
            ErrorDetail errorDetail = createErrorDetail(throwable);
            HttpStatus httpStatus = HttpStatus.resolve(problemDetail.getStatus());

            if (httpStatus == null || httpStatus.is5xxServerError()) {
                logServerError(requestInfo, httpStatus, errorDetail, throwable);
                return;
            }

            logClientError(problemDetail, requestInfo, errorDetail);
        } catch (Exception e) {
            log.error("Error 로깅 중 예외가 발생했습니다.", e);
        } finally {
            MDC.clear();
        }
    }

    private Optional<Throwable> extractThrowableFromArgs(Object[] args) {
        return Arrays.stream(args)
                .filter(Throwable.class::isInstance)
                .map(Throwable.class::cast)
                .findFirst();
    }

    private ErrorDetail createErrorDetail(Optional<Throwable> throwable) {
        return throwable.map(ErrorDetail::fromThrowable)
                .orElseGet(ErrorDetail::emptyErrorDetail);
    }

    private void logServerError(RequestInfo requestInfo, HttpStatus httpStatus,
                                ErrorDetail errorDetail, Optional<Throwable> throwable) {
        ErrorLog errorLog = ErrorLog.of("Error", LocalDateTime.now(), requestInfo, httpStatus, errorDetail);
        log.error(logMessageGenerator.convertToPrettyJson(errorLog));
        if (throwable.isPresent()) {
            errorLogger.error(errorLog, throwable.get());
            return;
        }
        errorLogger.error(errorLog);
    }

    private void logClientError(ProblemDetail problemDetail, RequestInfo requestInfo, ErrorDetail errorDetail) {
        ErrorLog errorLog = ErrorLog.of("Warn", LocalDateTime.now(), requestInfo,
                HttpStatus.resolve(problemDetail.getStatus()),
                errorDetail);
        log.warn(logMessageGenerator.convertToPrettyJson(errorLog));
    }
}
