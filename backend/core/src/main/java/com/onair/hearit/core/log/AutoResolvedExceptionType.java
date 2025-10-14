package com.onair.hearit.core.log;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Spring의 {@link org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver}에 의해 자동으로 처리되는 HTTP 상태 코드를
 * 나타내는 열거형이다.
 * <p>
 * {@code DefaultHandlerExceptionResolver}는 Spring MVC 요청 처리 과정 중 발생하는 일부 표준 예외를 가로채어, 예외를 다시 던지지 않고 적절한
 * {@link org.springframework.http.HttpStatus} 응답 코드로 변환한다.
 * <p>
 * 이 enum은 위와 같은 Spring 내부 자동 변환 예외들을 통합적으로 관리하고 로그 처리 시 일관된 형태로 사용할 수 있도록 정의된다.
 */
@Getter
@RequiredArgsConstructor
public enum AutoResolvedExceptionType {

    // DispatcherServlet이 핸들러를 찾지 못한 경우
    NOT_FOUND(HttpStatus.NOT_FOUND, "리소스를 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "요청한 HTTP Method를 지원하지 않습니다."),

    // DefaultHandlerExceptionResolver가 처리하는 Spring 내부 예외들
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다. (파라미터 바인딩 실패, 타입 불일치 등)"),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "지원하지 않는 Content-Type입니다."),
    NOT_ACCEPTABLE(HttpStatus.NOT_ACCEPTABLE, "요청한 Accept 헤더에 맞는 응답을 생성할 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "서비스를 일시적으로 사용할 수 없습니다."),
    NOT_IMPLEMENTED(HttpStatus.NOT_IMPLEMENTED, "요청한 기능이 구현되지 않았습니다."),
    DEFAULT(HttpStatus.INTERNAL_SERVER_ERROR, "오류가 발생했습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String message;

    public static AutoResolvedExceptionType from(HttpStatus httpStatus) {
        for (AutoResolvedExceptionType type : AutoResolvedExceptionType.values()) {
            if (type.getHttpStatus().equals(httpStatus)) {
                return type;
            }
        }
        return DEFAULT;
    }
}
