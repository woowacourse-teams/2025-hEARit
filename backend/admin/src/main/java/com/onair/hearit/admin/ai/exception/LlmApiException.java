package com.onair.hearit.admin.ai.exception;

import com.onair.hearit.admin.exception.AdminErrorCode;
import com.onair.hearit.admin.exception.custom.AdminException;

public class LlmApiException extends AdminException {

    public LlmApiException(String detail) {
        super(AdminErrorCode.AI_API_FAILED, detail);
    }

    public LlmApiException(String detail, Throwable cause) {
        super(AdminErrorCode.AI_API_FAILED, detail);
        initCause(cause);
    }

    public LlmApiException(AdminErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }

    public LlmApiException(AdminErrorCode errorCode, String detail, Throwable cause) {
        super(errorCode, detail);
        initCause(cause);
    }
}
