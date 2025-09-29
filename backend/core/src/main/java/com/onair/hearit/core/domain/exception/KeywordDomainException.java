package com.onair.hearit.core.domain.exception;

public class KeywordDomainException extends DomainException {
    public KeywordDomainException(String message) {
        super("KEYWORD_DOMAIN_ERROR", message);
    }
}
