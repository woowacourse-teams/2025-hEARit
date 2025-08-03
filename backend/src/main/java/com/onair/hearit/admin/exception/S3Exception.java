package com.onair.hearit.admin.exception;

public class S3Exception extends RuntimeException {

    public S3Exception(String message, Throwable e) {
        super(message, e);
    }
}
