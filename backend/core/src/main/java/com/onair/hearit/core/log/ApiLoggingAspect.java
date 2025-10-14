package com.onair.hearit.core.log;

import com.onair.hearit.core.log.logger.ConsoleLogger;
import com.onair.hearit.core.log.logger.JsonLogger;
import com.onair.hearit.core.log.property.api.ExceptionLogProperty;
import com.onair.hearit.core.log.property.api.RequestLogProperty;
import com.onair.hearit.core.log.property.api.ResponseLogProperty;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
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
@Component
@RequiredArgsConstructor
public class ApiLoggingAspect {

    private static final String LOGGED_BY_AOP = "loggedByAop";
    private static final String TRUE = "true";

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
    public void logRequest(JoinPoint joinPoint) {
        MDC.put(LOGGED_BY_AOP, TRUE);
        HttpServletRequest httpServletRequest = getHttpServletRequest();
        RequestLogProperty requestLogProperty = RequestLogProperty.of(httpServletRequest, joinPoint);
        jsonLogger.info(requestLogProperty);
        consoleLogger.info(requestLogProperty);
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
        jsonLogger.info(responseLogProperty);
        consoleLogger.info(responseLogProperty);
    }

    @AfterReturning(value = "exceptionHandler()", returning = "problemDetail")
    public void logExceptionHandler(JoinPoint joinPoint, ProblemDetail problemDetail) {
        if (!TRUE.equals(MDC.get(LOGGED_BY_AOP))) {
            logRequest(joinPoint);
            MDC.put(LOGGED_BY_AOP, TRUE);
        }
        try {
            HttpServletRequest httpServletRequest = getHttpServletRequest();
            String endPoint = httpServletRequest.getRequestURI();
            String method = httpServletRequest.getMethod();
            Optional<Throwable> throwable = extractThrowableFromArgs(joinPoint.getArgs());
            HttpStatus httpStatus = HttpStatus.resolve(problemDetail.getStatus());

            if (httpStatus == null || httpStatus.is5xxServerError()) {
                if (throwable.isPresent()) {
                    logServerErrorWithStackTrace(endPoint, method, httpStatus, throwable.get());
                    return;
                }
                logServerErrorWithoutStackTrace(endPoint, method, httpStatus);
                return;
            }
            if (throwable.isPresent()) {
                logClientErrorWithThrowable(endPoint, method, problemDetail, throwable.get());
                return;
            }
            logClientErrorWithoutThrowable(endPoint, method, problemDetail);
        } catch (Exception e) {
            jsonLogger.error("Error 로깅 중 예외가 발생했습니다.", e);
        }
    }

    private Optional<Throwable> extractThrowableFromArgs(Object[] args) {
        return Arrays.stream(args)
                .filter(Throwable.class::isInstance)
                .map(Throwable.class::cast)
                .findFirst();
    }

    private void logServerErrorWithStackTrace(String endPoint, String method, HttpStatus httpStatus,
                                              Throwable throwable) {
        ExceptionLogProperty exceptionLogProperty = ExceptionLogProperty.errorFromThrowable(endPoint, method,
                httpStatus, throwable);
        jsonLogger.error(exceptionLogProperty, throwable);
        consoleLogger.error(exceptionLogProperty);
    }

    private void logServerErrorWithoutStackTrace(String endPoint, String method, HttpStatus httpStatus) {
        ExceptionLogProperty exceptionLogProperty = ExceptionLogProperty.errorWithoutThrowable(endPoint, method,
                httpStatus);
        jsonLogger.error(exceptionLogProperty);
        consoleLogger.error(exceptionLogProperty);
    }

    private void logClientErrorWithThrowable(String endPoint, String method, ProblemDetail problemDetail,
                                             Throwable throwable) {
        ExceptionLogProperty exceptionLogProperty = ExceptionLogProperty.warnFromThrowable(endPoint, method,
                problemDetail, throwable);
        jsonLogger.warn(exceptionLogProperty);
        consoleLogger.warn(exceptionLogProperty);
    }

    private void logClientErrorWithoutThrowable(String endPoint, String method, ProblemDetail problemDetail) {
        ExceptionLogProperty exceptionLogProperty = ExceptionLogProperty.warnFromProblemDetail(endPoint, method,
                problemDetail);
        jsonLogger.warn(exceptionLogProperty);
        consoleLogger.warn(exceptionLogProperty);
    }
}
