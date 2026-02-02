package com.onair.hearit.core.domain.exception;

public class AdvertisementDomainException extends DomainException {

    public AdvertisementDomainException(String message) {
        super(DomainErrorCode.ADVERTISEMENT_DOMAIN_ERROR, message);
    }
}
