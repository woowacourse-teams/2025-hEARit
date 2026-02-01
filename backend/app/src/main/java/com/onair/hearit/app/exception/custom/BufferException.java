package com.onair.hearit.app.exception.custom;

import com.onair.hearit.app.exception.ErrorCode;

public class BufferException extends HearitException {

    public BufferException(String detail) {
        super(ErrorCode.BUFFER_ERROR, detail);
    }
}
