package com.onair.hearit.app.exception;

import com.onair.hearit.app.exception.custom.HearitException;
import com.onair.hearit.core.domain.exception.DomainException;
import com.onair.hearit.core.exception.DomainExceptionMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RequiredArgsConstructor
@RestControllerAdvice(basePackages = "com.onair.hearit.app")
public class ApiGlobalExceptionHandler {

    private final DomainExceptionMapper domainExceptionMapper;

    @ExceptionHandler(DomainException.class)
    public ProblemDetail handle(DomainException ex, HttpServletRequest request) {
        HttpStatus status = domainExceptionMapper.toHttpStatus(ex.getErrorCode());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        problemDetail.setTitle(ex.getErrorCode().name());
        problemDetail.setType(URI.create(request.getRequestURI()));
        return problemDetail;
    }

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
    public ResponseEntity<Object> handleNoResourceFoundException(NoResourceFoundException e,
                                                                 HttpServletRequest request) {
        if (request.getRequestURI().contains("/.well-known/appspecific")) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.status(404).build();
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        return buildProblemDetail(ErrorCode.INVALID_INPUT, "잘못된 파라미터 값: " + ex.getValue(), request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail handleMissingParam(MissingServletRequestParameterException ex, HttpServletRequest request) {
        return buildProblemDetail(ErrorCode.INVALID_INPUT, "필수 파라미터가 누락되었습니다: " + ex.getParameterName(), request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        return buildProblemDetail(ErrorCode.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP 메서드입니다.", request);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnhandledException(Exception ex, HttpServletRequest request) {
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
