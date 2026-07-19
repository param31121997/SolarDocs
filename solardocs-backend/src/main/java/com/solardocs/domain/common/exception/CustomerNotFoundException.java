package com.solardocs.domain.common.exception;

public class CustomerNotFoundException extends DomainException {
    public CustomerNotFoundException(String customerId) {
        super("CUSTOMER_NOT_FOUND", "Customer " + customerId + " not found");
    }
}
