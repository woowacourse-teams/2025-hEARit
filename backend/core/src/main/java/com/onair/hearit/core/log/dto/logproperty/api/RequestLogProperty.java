package com.onair.hearit.core.log.dto.logproperty.api;

import com.onair.hearit.core.log.dto.LogEvent;
import com.onair.hearit.core.log.dto.logproperty.LogProperty;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.web.bind.annotation.RequestBody;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestLogProperty implements LogProperty {

    private final String endPoint;
    private final String method;
    private final Map<String, List<String>> requestParameter;
    private final Object requestBody;

    public static RequestLogProperty of(HttpServletRequest httpServletRequest, JoinPoint joinPoint) {
        String endPoint = httpServletRequest.getRequestURI();
        String method = httpServletRequest.getMethod();
        Map<String, List<String>> parameters = getParameters(httpServletRequest);
        Object requestBody = extractRequestBody(joinPoint);
        return new RequestLogProperty(endPoint, method, parameters, requestBody);
    }

    private static Map<String, List<String>> getParameters(HttpServletRequest httpServletRequest) {
        Map<String, String[]> rawParameters = httpServletRequest.getParameterMap();
        return rawParameters.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> Arrays.asList(entry.getValue())
                ));
    }

    private static Object extractRequestBody(JoinPoint joinPoint) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameterAnnotations.length; i++) {
            for (Annotation annotation : parameterAnnotations[i]) {
                if (annotation.annotationType() == RequestBody.class) {
                    return args[i];
                }
            }
        }
        return null;
    }

    public static RequestLogProperty forFilter(HttpServletRequest request) {
        /* filter는 request body 추출 불가*/
        return new RequestLogProperty(
                request.getRequestURI(),
                request.getMethod(),
                getParameters(request),
                null);
    }

    @Override
    public String getEventName() {
        return LogEvent.REQUEST.getEventName();
    }
}
