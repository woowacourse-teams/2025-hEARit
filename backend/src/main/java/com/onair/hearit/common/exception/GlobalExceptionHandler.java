package com.onair.hearit.common.exception;

import com.onair.hearit.common.exception.custom.HearitException;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HearitException.class)
    public ProblemDetail handleHearitException(HearitException ex, HttpServletRequest request) {
        return buildProblemDetail(ex.getErrorCode(), ex.getDetail(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String detail = extractValidationDetail(ex);
        return buildProblemDetail(ErrorCode.INVALID_INPUT, detail, request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Object> handleNoResourceFoundException(NoResourceFoundException e, HttpServletRequest request) {
        if (request.getRequestURI().contains("/.well-known/appspecific")) {
            return ResponseEntity.notFound().build();
        }
        log.error("리소스를 찾을 수 없음", e);
        return ResponseEntity.status(404).build();
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnhandledException(Exception ex, HttpServletRequest request) {
        log.error("서버 내부 오류 - " + ex.getMessage(), ex);
        return buildProblemDetail(ErrorCode.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR.getTitle(), request);
    }

    private String extractValidationDetail(MethodArgumentNotValidException ex) {
        return ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
    }

    private ProblemDetail buildProblemDetail(ErrorCode errorCode, String detail, HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(errorCode.getHttpStatus(), detail);
        problemDetail.setTitle(errorCode.getTitle());
        problemDetail.setType(URI.create(request.getRequestURI()));
        return problemDetail;
    }
}
