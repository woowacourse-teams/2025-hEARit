package com.onair.hearit.app.exception.custom;

import com.onair.hearit.app.exception.ErrorCode;

public class BufferRequestException extends HearitException {

    public BufferRequestException(String detail) {
        super(ErrorCode.BUFFER_OVERFLOW, detail);
    }
}
