package com.onair.hearit.app.exception.custom;

import com.onair.hearit.app.exception.ErrorCode;

public class BufferOverflowException extends HearitException {

    public BufferOverflowException(String detail) {
        super(ErrorCode.BUFFER_OVERFLOW, detail);
    }
}
