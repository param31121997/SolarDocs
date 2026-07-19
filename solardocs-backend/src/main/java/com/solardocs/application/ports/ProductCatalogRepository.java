package com.solardocs.application.ports;

import com.solardocs.domain.vendor.Product;

import java.util.List;

public interface ProductCatalogRepository {
    List<Product> findAll();
    void saveAll(List<Product> products);
}
