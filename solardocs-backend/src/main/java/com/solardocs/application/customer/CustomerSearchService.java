package com.solardocs.application.customer;

import com.solardocs.application.ports.CustomerIndexRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerSearchService {

    private final CustomerIndexRepository indexRepository;

    public CustomerSearchService(CustomerIndexRepository indexRepository) {
        this.indexRepository = indexRepository;
    }

    public List<CustomerIndexRepository.IndexEntry> search(String q, String status, String village, String district) {
        return indexRepository.search(q, status, village, district);
    }
}
