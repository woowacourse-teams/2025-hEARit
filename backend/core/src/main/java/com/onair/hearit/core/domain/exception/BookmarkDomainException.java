package com.onair.hearit.core.domain.exception;

public class BookmarkDomainException extends DomainException {
    public BookmarkDomainException(String message) {
        super("BOOKMARK_DOMAIN_ERROR", message);
    }
}
