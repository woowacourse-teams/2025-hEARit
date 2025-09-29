package com.onair.hearit.core.domain.exception;

public class RecommendHearitDomainException extends DomainException {

    public RecommendHearitDomainException(String message) {
        super(DomainErrorCode.RECOMMEND_HEARIT_DOMAIN_ERROR, message);
    }
}
