package com.onair.hearit.admin.exception.custom;

import com.onair.hearit.admin.exception.AdminErrorCode;

public class AdminFileException extends AdminException {

    public AdminFileException(String detail) {
        super(AdminErrorCode.FILE_FAILED, detail);
    }
}
