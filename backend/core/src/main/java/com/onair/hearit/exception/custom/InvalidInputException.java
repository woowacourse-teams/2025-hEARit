package com.onair.hearit.exception.custom;

import com.onair.hearit.exception.ErrorCode;

public class InvalidInputException extends HearitException {

    public InvalidInputException(String detail) {
        super(ErrorCode.INVALID_INPUT, detail);
    }
}
