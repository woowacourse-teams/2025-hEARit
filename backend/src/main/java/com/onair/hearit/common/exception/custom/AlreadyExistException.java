package com.onair.hearit.common.exception.custom;

import com.onair.hearit.common.exception.ErrorCode;

public class AlreadyExistException extends HearitException {

    public AlreadyExistException(String detail) {
        super(ErrorCode.ALREADY_EXIST, detail);
    }
}
