package com.onair.hearit.core.domain.exception;

public class UserInfoDomainException extends DomainException {

    public UserInfoDomainException(String message) {
        super(DomainErrorCode.USERINFO_DOMAIN_ERROR, message);
    }
}
