package com.solardocs.domain.customer;

/** Value object. Format: A00001, A00002, ... */
public record CustomerId(String value) {
    public CustomerId {
        if (value == null || !value.matches("A\\d{5}")) {
            throw new IllegalArgumentException("Invalid CustomerId: " + value);
        }
    }
    public static CustomerId of(int sequence) {
        return new CustomerId("A" + String.format("%05d", sequence));
    }
}
