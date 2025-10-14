package com.onair.hearit.admin.exception.custom;

import com.onair.hearit.admin.exception.AdminErrorCode;

public class AdminNotFoundException extends AdminException {

    private AdminNotFoundException(String detail) {
        super(AdminErrorCode.NOT_FOUND, detail);
    }

    public AdminNotFoundException(String fieldName, String fieldValue) {
        this(String.format("%s을(를) 찾을 수 없습니다. 입력값: %s", fieldName, fieldValue));
    }
}
