package com.onair.hearit.exception.custom;

import com.onair.hearit.exception.ErrorCode;

public class AlreadyExistException extends HearitException {

    public AlreadyExistException(String detail) {
        super(ErrorCode.ALREADY_EXIST, detail);
    }
}
