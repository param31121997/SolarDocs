package com.solardocs.application.ports;

import com.solardocs.domain.customer.Customer;
import com.solardocs.domain.customer.CustomerId;

import java.util.Optional;

public interface CustomerRepository {
    void save(Customer customer);
    Optional<Customer> findById(CustomerId id);
    boolean existsById(CustomerId id);
}
