package com.solardocs.infrastructure.pdf.strategy;

import com.solardocs.domain.customer.Customer;

import java.util.Map;

public interface DocumentGenerationStrategy {
    /** The template code this strategy handles, e.g. "QUOTATION". */
    String templateCode();

    /** Builds the data model the HTML template will be rendered with. */
    Map<String, Object> buildModel(Customer customer, Map<String, Object> extraFields);
}
