package com.onair.hearit.admin.ai.exception;

import com.onair.hearit.admin.exception.AdminErrorCode;
import com.onair.hearit.admin.exception.custom.AdminException;

public class AudioProcessingException extends AdminException {

    public AudioProcessingException(String detail) {
        super(AdminErrorCode.AI_PROCESSING_FAILED, detail);
    }

    public AudioProcessingException(String detail, Throwable cause) {
        super(AdminErrorCode.AI_PROCESSING_FAILED, detail);
        initCause(cause);
    }

    public static AudioProcessingException invalidFile(String detail) {
        return new AudioProcessingException(detail) {
            @Override
            public AdminErrorCode getErrorCode() {
                return AdminErrorCode.AI_INVALID_FILE;
            }
        };
    }

    public static AudioProcessingException fileTooLarge(String detail) {
        return new AudioProcessingException(detail) {
            @Override
            public AdminErrorCode getErrorCode() {
                return AdminErrorCode.AI_FILE_TOO_LARGE;
            }
        };
    }

    public static AudioProcessingException unsupportedFormat(String detail) {
        return new AudioProcessingException(detail) {
            @Override
            public AdminErrorCode getErrorCode() {
                return AdminErrorCode.AI_UNSUPPORTED_FORMAT;
            }
        };
    }

    public static AudioProcessingException apiCallFailed(String detail, Throwable cause) {
        AudioProcessingException ex = new AudioProcessingException(detail) {
            @Override
            public AdminErrorCode getErrorCode() {
                return AdminErrorCode.AI_API_FAILED;
            }
        };
        ex.initCause(cause);
        return ex;
    }
}
