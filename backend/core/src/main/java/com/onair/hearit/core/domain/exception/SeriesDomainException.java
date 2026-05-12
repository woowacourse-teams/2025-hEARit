package com.onair.hearit.core.domain.exception;

public class SeriesDomainException extends DomainException {

    public SeriesDomainException(String message) {
        super(DomainErrorCode.SERIES_DOMAIN_ERROR, message);
    }
}
