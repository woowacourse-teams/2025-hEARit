package com.onair.hearit.core.domain.exception;

public class PlayingHistoryDomainException extends DomainException {
    public PlayingHistoryDomainException(String message) {
        super(DomainErrorCode.PLAYING_HISTORY_DOMAIN_ERROR, message);
    }
}
