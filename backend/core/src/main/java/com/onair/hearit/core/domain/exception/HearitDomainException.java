package com.onair.hearit.core.domain.exception;

public class HearitDomainException extends DomainException {
    public HearitDomainException(String message) {
        super("HEARIT_DOMAIN_ERROR", message);
    }
}
