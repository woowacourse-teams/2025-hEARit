package com.onair.hearit.app.exception.custom;

import com.onair.hearit.app.exception.ErrorCode;

public class NotFoundException extends HearitException {

    private NotFoundException(String detail) {
        super(ErrorCode.NOT_FOUND, detail);
    }

    public NotFoundException(String fieldName, String fieldValue) {
        this(String.format("%s을(를) 찾을 수 없습니다. 입력값: %s", fieldName, fieldValue));
    }
}
