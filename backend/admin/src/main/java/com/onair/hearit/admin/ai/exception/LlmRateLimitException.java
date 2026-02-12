package com.onair.hearit.admin.ai.exception;

import com.onair.hearit.admin.exception.AdminErrorCode;

public class LlmRateLimitException extends LlmApiException {

    public LlmRateLimitException(String detail) {
        super(AdminErrorCode.AI_RATE_LIMITED, detail);
    }

    public LlmRateLimitException(String detail, Throwable cause) {
        super(AdminErrorCode.AI_RATE_LIMITED, detail, cause);
    }

    public boolean isRetryable() {
        return true;
    }
}
