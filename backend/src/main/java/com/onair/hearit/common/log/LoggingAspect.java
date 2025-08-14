package com.onair.hearit.common.log;

import com.onair.hearit.common.log.mask.MaskingSupport;
import com.onair.hearit.common.log.message.dto.ExceptionLog;
import com.onair.hearit.common.log.message.dto.ExceptionLog.ErrorDetail;
import com.onair.hearit.common.log.message.dto.RequestInfo;
import com.onair.hearit.common.log.message.dto.RequestLog;
import com.onair.hearit.common.log.message.dto.ResponseLog;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
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

    private final Logger errorLogger = LogManager.getLogger("errorLogger");
    private static final Logger consoleLogger = LogManager.getLogger("consoleLogger");
    private static final Logger jsonLogger = LogManager.getLogger("jsonLogger");
    private final MaskingSupport maskingSupport;

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
        RequestLog requestLog = getRequestLog(joinPoint);
        jsonLogger.info(maskingSupport.mask(requestLog));
        consoleLogger.info(getRequestLogForConsole(requestLog));
    }

    private RequestLog getRequestLog(JoinPoint joinPoint) {
        HttpServletRequest request = getHttpServletRequest();
        RequestInfo requestInfo = RequestInfo.fromMdc();
        Object requestBody = extractRequestBody(joinPoint);
        return RequestLog.of(
                LocalDateTime.now(),
                requestInfo,
                request.getParameterMap(),
                requestBody);
    }

    private HttpServletRequest getHttpServletRequest() {
        return Optional.of(RequestContextHolder.getRequestAttributes())
                .filter(ServletRequestAttributes.class::isInstance)
                .map(ServletRequestAttributes.class::cast)
                .map(ServletRequestAttributes::getRequest)
                .orElseThrow(() -> new IllegalStateException("현재 스레드에 바인딩 된 request가 없습니다."));
    }

    private Object extractRequestBody(JoinPoint joinPoint) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameterAnnotations.length; i++) {
            for (Annotation annotation : parameterAnnotations[i]) {
                if (annotation.annotationType().getSimpleName().equals("RequestBody")) {
                    return args[i];
                }
            }
        }
        return null;
    }

    @AfterReturning(value = "allMapping()", returning = "responseEntity")
    public void logResponse(JoinPoint joinPoint, ResponseEntity<?> responseEntity) {
        RequestInfo requestInfo = RequestInfo.fromMdc();
        ResponseLog responseLog = ResponseLog.of(
                LocalDateTime.now(),
                requestInfo,
                responseEntity,
                calculateTimeTakenMs());
        jsonLogger.info(maskingSupport.mask(responseLog));
        consoleLogger.info(formatResponseLogForConsole(responseLog));
    }

    private long calculateTimeTakenMs() {
        return Optional.ofNullable(MDC.get("startTime"))
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
        jsonLogger.error(maskingSupport.mask(exceptionLog));
        errorLogger.error(exceptionLog, throwable);
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
        jsonLogger.error(maskingSupport.mask(exceptionLog));
        errorLogger.error(exceptionLog);
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
        jsonLogger.warn(maskingSupport.mask(exceptionLog));
        consoleLogger.warn(
                "[CLIENT ERROR] {} {} from {} → status: {} / title: {} / detail: {}",
                requestInfo.getHttpMethod(),
                requestInfo.getRequestUri(),
                requestInfo.getIp(),
                problemDetail.getStatus(),
                problemDetail.getTitle(),
                problemDetail.getDetail()
        );
    }

    private String getRequestLogForConsole(RequestLog requestLog) {
        String method = requestLog.getRequestInfo().getHttpMethod();
        String uri = requestLog.getRequestInfo().getRequestUri();
        String ip = requestLog.getRequestInfo().getIp();
        String time = requestLog.getTimestamp();
        Map<String, List<String>> params = requestLog.getRequestParameter();
        Object body = requestLog.getRequestBody();

        return String.format("[REQUEST] %s → %s %s from %s params=%s body=%s",
                time,
                method,
                uri,
                ip,
                toFlatParamString(params),
                body == null ? "null" : truncateBody(body.toString())
        );
    }

    private String toFlatParamString(Map<String, List<String>> params) {
        return params.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(", ", "{", "}"));
    }

    private String truncateBody(String body) {
        return body.length() > 200 ? body.substring(0, 200) + "...(생ㅜ)" : body;
    }

    private String formatResponseLogForConsole(ResponseLog<?> responseLog) {
        String time = responseLog.timestamp();
        String method = responseLog.requestInfo().getHttpMethod();
        String uri = responseLog.requestInfo().getRequestUri();
        String ip = responseLog.requestInfo().getIp();
        long timeTaken = responseLog.timeTakenMs();
        Object body = responseLog.responseEntity();

        return String.format(
                "[RESPONSE] %s ← %s %s from %s timeTaken=%dms body=%s",
                time,
                method,
                uri,
                ip,
                timeTaken,
                truncateBody(body == null ? "null" : body.toString())
        );
    }
}
