package com.onair.hearit.common.exception.custom;

import com.onair.hearit.common.exception.ErrorCode;

public class InvalidInputException extends HearitException {

    public InvalidInputException(String detail) {
        super(ErrorCode.INVALID_INPUT, detail);
    }
}
