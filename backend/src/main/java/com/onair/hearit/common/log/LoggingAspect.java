package com.onair.hearit.common.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onair.hearit.common.log.message.ErrorLog;
import com.onair.hearit.common.log.message.ErrorLog.ErrorDetail;
import com.onair.hearit.common.log.message.RequestInfo;
import com.onair.hearit.common.log.message.RequestLog;
import com.onair.hearit.common.log.message.ResponseLog;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private final LogMessageGenerator logMessageGenerator;

    private static long calculateTimeTakenMs() {
        String startTime = MDC.get("startTime");
        return System.currentTimeMillis() - Long.parseLong(startTime);
    }

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
        long startTime = System.currentTimeMillis();
        MDC.put("startTime", String.valueOf(startTime));
        RequestLog requestLog = getRequestLog(joinPoint);

        log.info("{}", logMessageGenerator.getLogMessage(requestLog));
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
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (servletRequestAttributes == null) {
            throw new IllegalStateException("현재 스레드에 바인딩 된 request가 없습니다.");
        }
        return servletRequestAttributes.getRequest();
    }

    private void putRequestInfoToMdc(RequestInfo requestInfo) {
        MDC.put("id", requestInfo.id());
        MDC.put("ip", requestInfo.ip());
        MDC.put("httpMethod", requestInfo.httpMethod());
        MDC.put("requestUri", requestInfo.requestUri());
    }

    private Object extractRequestBody(JoinPoint joinPoint) {
        return Arrays.stream(joinPoint.getArgs())
                .filter(arg -> !(arg instanceof HttpServletRequest) && !(arg instanceof HttpServletResponse))
                .map(this::maskSensitiveFields)
                .findFirst()
                .orElse(null);
    }

    private Object maskSensitiveFields(Object requestBody) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(requestBody);
            // Mask password, pwd, pass fields
            json = json.replaceAll("(?i)(\"password\"\\s*:\\s*\")[^\"]*(\")", "$1****$2");
            json = json.replaceAll("(?i)(\"pwd\"\\s*:\\s*\")[^\"]*(\")", "$1****$2");
            json = json.replaceAll("(?i)(\"pass\"\\s*:\\s*\")[^\"]*(\")", "$1****$2");
            return objectMapper.readValue(json, Object.class);
        } catch (Exception e) {
            return requestBody;
        }
    }

    @AfterReturning(value = "allMapping()", returning = "responseEntity")
    public void logResponse(ResponseEntity<?> responseEntity) {
        RequestInfo requestInfo = RequestInfo.getCurrentHttpRequest();
        ResponseLog responseLog = ResponseLog.of(
                LocalDateTime.now(),
                requestInfo,
                responseEntity,
                calculateTimeTakenMs());

        log.info("{}", logMessageGenerator.getLogMessage(responseLog));
        MDC.clear();
    }

    @AfterReturning(value = "exceptionHandler()", returning = "problemDetail")
    public void logErrorResponse(JoinPoint joinPoint, ProblemDetail problemDetail) {
        RequestInfo requestInfo = RequestInfo.getCurrentHttpRequest();
        Optional<Throwable> optionalThrowable = Arrays.stream(joinPoint.getArgs())
                .filter(Throwable.class::isInstance)
                .map(Throwable.class::cast)
                .findFirst();
        ErrorDetail errorDetail = optionalThrowable.map(ErrorDetail::fromThrowable)
                .orElseGet(ErrorDetail::emptyErrorDetail);
        ErrorLog errorLog = ErrorLog.of(LocalDateTime.now(), requestInfo, HttpStatus.resolve(problemDetail.getStatus()),
                errorDetail);

        log.error("{}", logMessageGenerator.getLogMessage(errorLog));
    }
}
