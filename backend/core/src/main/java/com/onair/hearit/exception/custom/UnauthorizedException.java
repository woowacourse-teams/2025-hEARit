package com.onair.hearit.exception.custom;

import com.onair.hearit.exception.ErrorCode;

public class UnauthorizedException extends HearitException {

    public UnauthorizedException(String detail) {
        super(ErrorCode.UNAUTHORIZED, detail);
    }
}
