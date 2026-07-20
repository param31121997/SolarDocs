package com.solardocs.domain.common.exception;

public class DuplicateItemNameException extends DomainException {
    public DuplicateItemNameException(String itemName) {
        super("ITEM_NAME_DUPLICATE", "An item named '" + itemName + "' already exists");
    }
}
