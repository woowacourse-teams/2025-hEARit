package com.onair.hearit.core.domain.exception;

public class UserInfoDomainException extends DomainException {

    public UserInfoDomainException(String message) {
        super("USERINFO_DOMAIN_ERROR", message);
    }
}
