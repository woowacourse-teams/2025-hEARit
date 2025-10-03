package com.onair.hearit.core.domain.exception;

public class MemberDomainException extends DomainException {
    public MemberDomainException(String message) {
        super(DomainErrorCode.MEMBER_DOMAIN_ERROR, message);
    }
}
