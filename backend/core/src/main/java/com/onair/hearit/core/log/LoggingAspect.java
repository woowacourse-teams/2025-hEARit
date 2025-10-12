package com.onair.hearit.core.log;

import com.onair.hearit.core.log.dto.ExceptionLog;
import com.onair.hearit.core.log.dto.ExceptionLog.ErrorDetail;
import com.onair.hearit.core.log.dto.LogFormat;
import com.onair.hearit.core.log.dto.RequestInfo;
import com.onair.hearit.core.log.dto.RequestLogProperty;
import com.onair.hearit.core.log.dto.ResponseLogProperty;
import com.onair.hearit.core.log.logger.ConsoleLogger;
import com.onair.hearit.core.log.logger.JsonLogger;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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
@Log4j2
@Component
@RequiredArgsConstructor
public class LoggingAspect {

    private final ConsoleLogger consoleLogger;
    private final JsonLogger jsonLogger;

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

    @Pointcut("(getMapping() || postMapping() || deleteMapping() || putMapping() || patchMapping())"
            + "&& !within(com.onair.hearit.admin..*)")
    public void allMapping() {
    }

    @Pointcut("(@within(org.springframework.web.bind.annotation.RestControllerAdvice)" +
            "|| @within(org.springframework.web.bind.annotation.ControllerAdvice))" +
            "&& !within(com.onair.hearit.admin..*)")
    public void exceptionHandler() {
    }

    @Before("allMapping()")
    public void markAopEntered() {
        MDC.put("AOP_ENTERED", "true");
    }

    @Before("allMapping()")
    public void logRequest(JoinPoint joinPoint) {
        HttpServletRequest httpServletRequest = getHttpServletRequest();
        RequestLogProperty requestLogProperty = RequestLogProperty.of(httpServletRequest, joinPoint);
        LogFormat apiRequest = new LogFormat(requestLogProperty);
        consoleLogger.info(requestLogProperty);
        jsonLogger.info(apiRequest);
    }

    private HttpServletRequest getHttpServletRequest() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(ServletRequestAttributes.class::isInstance)
                .map(ServletRequestAttributes.class::cast)
                .map(ServletRequestAttributes::getRequest)
                .orElseThrow(() -> new IllegalStateException("현재 스레드에 바인딩 된 request가 없습니다."));
    }

    @AfterReturning(value = "allMapping()", returning = "responseEntity")
    public void logResponse(ResponseEntity<?> responseEntity) {
        HttpServletRequest httpServletRequest = getHttpServletRequest();
        String endPoint = httpServletRequest.getRequestURI();
        ResponseLogProperty responseLogProperty = ResponseLogProperty.of(endPoint, responseEntity);
        LogFormat apiResponse = new LogFormat(responseLogProperty);
        jsonLogger.info(apiResponse);
        consoleLogger.info(responseLogProperty);
    }

    @AfterReturning(value = "exceptionHandler()", returning = "problemDetail")
    public void logExceptionHandler(JoinPoint joinPoint, ProblemDetail problemDetail) {
        try {
            RequestInfo requestInfo = RequestInfo.fromMdc();
            Optional<Throwable> throwable = extractThrowableFromArgs(joinPoint.getArgs());
            ErrorDetail errorDetail = throwable.map(ErrorDetail::fromThrowable)
                    .orElseGet(ErrorDetail::emptyErrorDetail);
            HttpStatus httpStatus = HttpStatus.resolve(problemDetail.getStatus());

            if (httpStatus == null || httpStatus.is5xxServerError()) {
                if (throwable.isPresent()) {
                    logServerErrorWithStackTrace(requestInfo, httpStatus, errorDetail, throwable.get());
                    return;
                }
                logServerErrorWithoutStackTrace(requestInfo, httpStatus, errorDetail, throwable.get());
                return;
            }
            logClientError(problemDetail, requestInfo, errorDetail);
        } catch (Exception e) {
            log.error("Error 로깅 중 예외가 발생했습니다.", e);
        }
    }

    private Optional<Throwable> extractThrowableFromArgs(Object[] args) {
        return Arrays.stream(args)
                .filter(Throwable.class::isInstance)
                .map(Throwable.class::cast)
                .findFirst();
    }

    private void logServerErrorWithStackTrace(RequestInfo requestInfo, HttpStatus httpStatus,
                                              ErrorDetail errorDetail, Throwable throwable) {
        ExceptionLog exceptionLog = ExceptionLog.error(LocalDateTime.now(), requestInfo, httpStatus, errorDetail);
        jsonLogger.error(exceptionLog, throwable);
        consoleLogger.error("[ERROR] {} {} from {} → {}",
                requestInfo.getHttpMethod(),
                requestInfo.getRequestUri(),
                requestInfo.getIp(),
                throwable.toString(),
                throwable
        );
    }

    private void logServerErrorWithoutStackTrace(RequestInfo requestInfo, HttpStatus httpStatus,
                                                 ErrorDetail errorDetail, Throwable throwable) {
        ExceptionLog exceptionLog = ExceptionLog.error(LocalDateTime.now(), requestInfo, httpStatus, errorDetail);
        jsonLogger.error(exceptionLog);
        consoleLogger.error("[ERROR] {} {} from {} → {}",
                requestInfo.getHttpMethod(),
                requestInfo.getRequestUri(),
                requestInfo.getIp(),
                throwable.toString()
        );
    }

    private void logClientError(ProblemDetail problemDetail, RequestInfo requestInfo, ErrorDetail errorDetail) {
        ExceptionLog exceptionLog = ExceptionLog.warn(LocalDateTime.now(), requestInfo,
                HttpStatus.resolve(problemDetail.getStatus()),
                errorDetail);
        jsonLogger.warn(exceptionLog);
        consoleLogger.warn(
                "[WARN] {} {} from {} → status: {} / title: {} / detail: {}",
                requestInfo.getHttpMethod(),
                requestInfo.getRequestUri(),
                requestInfo.getIp(),
                problemDetail.getStatus(),
                problemDetail.getTitle(),
                problemDetail.getDetail()
        );
    }
}
