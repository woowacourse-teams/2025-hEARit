package com.onair.hearit.core.domain.exception;

public class CategoryDomainException extends DomainException {

    public CategoryDomainException(String message) {
        super("CATEGORY_DOMAIN_ERROR", message);
    }
}
