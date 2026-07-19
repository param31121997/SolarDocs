package com.solardocs.domain.common.exception;

public class DuplicateCategoryNameException extends DomainException {
    public DuplicateCategoryNameException(String categoryName) {
        super("CATEGORY_NAME_DUPLICATE", "A product category named '" + categoryName + "' already exists");
    }
}
