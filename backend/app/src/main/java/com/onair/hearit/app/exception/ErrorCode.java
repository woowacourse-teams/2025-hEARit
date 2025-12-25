package com.onair.hearit.app.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    //CLIENT ERROR
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 유효하지 않습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "해당 정보를 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP 메서드입니다."),
    ALREADY_EXIST(HttpStatus.CONFLICT, "이미 존재하는 데이터입니다."),
    BUFFER_OVERFLOW(HttpStatus.TOO_MANY_REQUESTS, "대기 중인 요청이 너무 많습니다."),

    //CLIENT UNAUTHORIZED ERROR
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증되지 않은 요청입니다."),
    AUTHENTICATION_REQUIRED(HttpStatus.UNAUTHORIZED, "인증이 필요한 요청입니다."),
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "엑세스 토큰이 유효하지 않습니다."),
    ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "엑세스 토큰이 만료되었습니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    //SERVER ERROR
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다."),
    OAUTH_CLIENT_ERROR(HttpStatus.BAD_GATEWAY, "OAuth 관련 에러입니다."),
    BUFFER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "버퍼 오류입니다.");

    private final HttpStatus httpStatus;
    private final String title;

    ErrorCode(HttpStatus httpStatus, String title) {
        this.httpStatus = httpStatus;
        this.title = title;
    }
}
