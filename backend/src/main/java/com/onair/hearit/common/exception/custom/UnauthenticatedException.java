package com.onair.hearit.common.exception.custom;

import com.onair.hearit.common.exception.ErrorCode;

public class UnauthenticatedException extends HearitException {

    public UnauthenticatedException() {
        super(ErrorCode.AUTHENTICATION_REQUIRED, "로그인한 회원이 아닙니다.");
    }
}
