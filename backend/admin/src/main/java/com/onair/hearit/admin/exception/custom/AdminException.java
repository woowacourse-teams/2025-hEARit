package com.onair.hearit.admin.exception.custom;

import com.onair.hearit.admin.exception.AdminErrorCode;
import lombok.Getter;

@Getter
public abstract class AdminException extends RuntimeException {

    private final AdminErrorCode errorCode;
    private final String detail;

    protected AdminException(AdminErrorCode errorCode, String detail) {
        super(errorCode.getTitle());
        this.errorCode = errorCode;
        this.detail = detail;
    }

    @Override
    public String getMessage() {
        return this.detail;
    }
}
