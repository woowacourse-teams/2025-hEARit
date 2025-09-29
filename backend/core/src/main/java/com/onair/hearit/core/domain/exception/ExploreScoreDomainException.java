package com.onair.hearit.core.domain.exception;

public class ExploreScoreDomainException extends DomainException {
    public ExploreScoreDomainException(String message) {
        super("EXPLORE_SCORE_DOMAIN_ERROR", message);
    }
}
