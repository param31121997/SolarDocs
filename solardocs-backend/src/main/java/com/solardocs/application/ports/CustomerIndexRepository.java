package com.solardocs.application.ports;

import java.util.List;

public interface CustomerIndexRepository {

    record IndexEntry(
            String customerId, String name, String mobile, String status,
            String folderPath, String village, String district,
            String consumerNumber, String applicationNumber
    ) {}

    List<IndexEntry> findAll();
    List<IndexEntry> search(String query, String status, String village, String district);
    void upsert(IndexEntry entry);
    void rebuildFrom(List<IndexEntry> entries);
}
