package com.onair.hearit.exception.custom;

import com.onair.hearit.exception.ErrorCode;

public class BufferRequestException extends HearitException {

    public BufferRequestException(String detail) {
        super(ErrorCode.BUFFER_OVERFLOW, detail);
    }
}
