package com.onair.hearit.exception.custom;

import com.onair.hearit.exception.AdminErrorCode;

public class AdminFileException extends AdminException {

    public AdminFileException(String detail) {
        super(AdminErrorCode.FILE_FAILED, detail);
    }
}
