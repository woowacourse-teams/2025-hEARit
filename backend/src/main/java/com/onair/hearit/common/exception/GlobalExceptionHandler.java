package com.onair.hearit.common.exception;

import com.onair.hearit.common.exception.custom.HearitException;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import org.springframework.http.ProblemDetail;
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
}
