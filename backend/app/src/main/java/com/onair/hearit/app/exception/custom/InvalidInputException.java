package com.onair.hearit.app.exception.custom;

import com.onair.hearit.app.exception.ErrorCode;

public class InvalidInputException extends HearitException {

    public InvalidInputException(String detail) {
        super(ErrorCode.INVALID_INPUT, detail);
    }
}
