package com.onair.hearit.core.domain.exception;

public class ReactionDomainException extends DomainException {

    public ReactionDomainException(String message) {
        super(DomainErrorCode.REACTION_DOMAIN_ERROR, message);
    }
}
