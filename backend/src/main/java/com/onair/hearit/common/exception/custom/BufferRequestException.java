package com.onair.hearit.common.exception.custom;

import com.onair.hearit.common.exception.ErrorCode;

public class BufferRequestException extends HearitException {

    public BufferRequestException(String detail) {
        super(ErrorCode.BUFFER_OVERFLOW, detail);
    }
}
