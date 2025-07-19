package com.onair.hearit.common.exception;

import com.onair.hearit.common.exception.custom.HearitException;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.stream.Collectors;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HearitException.class)
    public ProblemDetail handleHearitException(HearitException ex, HttpServletRequest request) {
        ErrorCode errorCode = ex.getErrorCode();
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(errorCode.getHttpStatus(), ex.getDetail());
        problemDetail.setTitle(errorCode.getTitle());
        problemDetail.setType(URI.create(request.getRequestURI()));
        return problemDetail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        ErrorCode errorCode = ErrorCode.INVALID_INPUT;

        String detail = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(errorCode.getHttpStatus(), detail);
        problemDetail.setTitle(errorCode.getTitle());
        problemDetail.setType(URI.create(request.getRequestURI()));
        return problemDetail;
    }
}
