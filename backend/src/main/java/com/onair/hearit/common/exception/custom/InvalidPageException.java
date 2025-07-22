package com.onair.hearit.common.exception.custom;

import com.onair.hearit.common.exception.ErrorCode;

public class InvalidPageException extends HearitException {

    private InvalidPageException(String detail) {
        super(ErrorCode.INVALID_INPUT, detail);
    }

    public InvalidPageException(int pageMin) {
        this(String.format("page는 %d 이상이어야 합니다.", pageMin));
    }

    public InvalidPageException(int sizeMin, int sizeMax) {
        this(String.format("size는 %d 이상 %d 이하이어야 합니다.", sizeMin, sizeMax));
    }
}
