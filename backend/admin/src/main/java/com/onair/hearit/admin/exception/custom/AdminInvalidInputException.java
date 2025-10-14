package com.onair.hearit.admin.exception.custom;

import com.onair.hearit.admin.exception.AdminErrorCode;

public class AdminInvalidInputException extends AdminException {

    public AdminInvalidInputException(String detail) {
        super(AdminErrorCode.INVALID_INPUT, detail);
    }
}
