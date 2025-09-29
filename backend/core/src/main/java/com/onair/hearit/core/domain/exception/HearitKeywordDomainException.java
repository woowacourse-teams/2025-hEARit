package com.onair.hearit.core.domain.exception;

public class HearitKeywordDomainException extends DomainException {
    public HearitKeywordDomainException(String message) {
        super("HEARIT_KEYWORD_DOMAIN_ERROR", message);
    }
}
