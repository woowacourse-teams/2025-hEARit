package com.onair.hearit.auth.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class KakaoExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(KakaoClientException.class)
    public ResponseEntity<Object> handleBadRequestException(final KakaoClientException e, final WebRequest request)
    {
        log.error(e.getMessage(), e);
        return buildResponseEntity(e, e.getStatusCode(), e.getMessage(), request);
    }

    private ResponseEntity<Object> buildResponseEntity(
            final Exception e,
            final HttpStatusCode status,
            final String message,
            final WebRequest request
    ) {
        final String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        final ProblemDetail body = super.createProblemDetail(e, status, message, path, null, request);
        return super.handleExceptionInternal(e, body, new HttpHeaders(), status, request);
    }
}
