package com.onair.hearit.core.domain.exception;

public class HearitDomainException extends DomainException {
    public HearitDomainException(String message) {
        super(DomainErrorCode.HEARIT_DOMAIN_ERROR, message);
    }
}
