package com.onair.hearit.core.domain.exception;

public class HearitClusterDomainException extends DomainException {
    public HearitClusterDomainException(String message) {
        super(DomainErrorCode.HEARIT_CLUSTER_DOMAIN_ERROR, message);
    }
}
