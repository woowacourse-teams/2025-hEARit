package com.onair.hearit.core.domain.exception;

public class HearitClusterDomainException extends DomainException {
    public HearitClusterDomainException(String message) {
        super(DomainErrorCode.EXPLORE_SCORE_DOMAIN_ERROR, message);
    }
}
