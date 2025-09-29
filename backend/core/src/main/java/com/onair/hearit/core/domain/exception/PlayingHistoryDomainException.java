package com.onair.hearit.core.domain.exception;

public class PlayingHistoryDomainException extends DomainException {
    public PlayingHistoryDomainException(String message) {
        super("PLAYING_HISTORY_DOMAIN_ERROR", message);
    }
}
