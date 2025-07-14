package com.onair.hearit.common.exception.custom;

import com.onair.hearit.common.exception.ErrorCode;
import lombok.Getter;

@Getter
public abstract class HearitException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String detail;

    protected HearitException(ErrorCode errorCode, String detail) {
        super(errorCode.getTitle());
        this.errorCode = errorCode;
        this.detail = detail;
    }
}
