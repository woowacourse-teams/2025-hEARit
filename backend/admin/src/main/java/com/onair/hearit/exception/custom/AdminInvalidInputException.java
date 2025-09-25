package com.onair.hearit.exception.custom;

import com.onair.hearit.exception.AdminErrorCode;

public class AdminInvalidInputException extends AdminException {

    public AdminInvalidInputException(String detail) {
        super(AdminErrorCode.INVALID_INPUT, detail);
    }
}
