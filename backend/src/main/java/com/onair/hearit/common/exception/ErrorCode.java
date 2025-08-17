package com.onair.hearit.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    //CLIENT ERROR
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 유효하지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증되지 않은 요청입니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "해당 정보를 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP 메서드입니다."),
    ALREADY_EXIST(HttpStatus.CONFLICT, "이미 존재하는 데이터입니다."),

    //SERVER ERROR
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다."),
    OAUTH_CLIENT_ERROR(HttpStatus.BAD_GATEWAY, "OAuth 관련 에러입니다.");

    private final HttpStatus httpStatus;
    private final String title;

    ErrorCode(HttpStatus httpStatus, String title) {
        this.httpStatus = httpStatus;
        this.title = title;
    }
}
