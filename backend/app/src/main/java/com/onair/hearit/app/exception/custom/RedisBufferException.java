package com.onair.hearit.app.exception.custom;

import com.onair.hearit.app.exception.ErrorCode;

/**
 * Redis 인프라 장애 전용 예외.
 *
 * <p>Redis 연결/시스템/접근 오류만 이 예외로 표현한다.
 * Circuit Breaker는 이 타입만 실패로 집계한다.</p>
 */
public class RedisBufferException extends HearitException {

    public RedisBufferException(String detail) {
        super(ErrorCode.REDIS_BUFFER_ERROR, detail);
    }

    public RedisBufferException(String detail, Throwable cause) {
        super(ErrorCode.REDIS_BUFFER_ERROR, detail);
        initCause(cause);
    }
}
