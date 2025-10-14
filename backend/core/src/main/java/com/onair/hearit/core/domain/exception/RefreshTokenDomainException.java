package com.onair.hearit.core.domain.exception;

public class RefreshTokenDomainException extends DomainException {

    public RefreshTokenDomainException(String message) {
        super(DomainErrorCode.REFRESH_TOKEN_DOMAIN_ERROR, message);
    }
}
