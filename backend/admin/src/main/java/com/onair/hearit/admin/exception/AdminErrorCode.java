package com.onair.hearit.admin.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AdminErrorCode {

    INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 유효하지 않습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "해당 정보를 찾을 수 없습니다."),
    FILE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 처리에 실패했습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP 메서드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다."),

    // AI 처리 관련
    AI_PROCESSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AI 처리에 실패했습니다."),
    AI_INVALID_FILE(HttpStatus.BAD_REQUEST, "유효하지 않은 파일입니다."),
    AI_FILE_TOO_LARGE(HttpStatus.BAD_REQUEST, "파일 크기가 제한을 초과했습니다."),
    AI_UNSUPPORTED_FORMAT(HttpStatus.BAD_REQUEST, "지원하지 않는 파일 형식입니다."),
    AI_API_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "외부 AI 서비스 호출에 실패했습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String title;

    AdminErrorCode(HttpStatus httpStatus, String title) {
        this.httpStatus = httpStatus;
        this.title = title;
    }
}
