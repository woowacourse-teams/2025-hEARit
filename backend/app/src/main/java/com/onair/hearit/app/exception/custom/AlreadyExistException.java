package com.onair.hearit.app.exception.custom;

import com.onair.hearit.app.exception.ErrorCode;

public class AlreadyExistException extends HearitException {

    public AlreadyExistException(String detail) {
        super(ErrorCode.ALREADY_EXIST, detail);
    }
}
