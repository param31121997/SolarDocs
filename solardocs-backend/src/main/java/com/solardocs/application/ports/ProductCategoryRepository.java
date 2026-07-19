package com.solardocs.application.ports;

import com.solardocs.domain.masterdata.ProductCategory;

import java.util.List;
import java.util.Optional;

public interface ProductCategoryRepository {

    List<ProductCategory> findAll();

    Optional<ProductCategory> findById(String id);

    Optional<ProductCategory> findByCategoryNameIgnoreCase(String categoryName);

    /** Upserts by id - insert on create, replace-in-place on update/activate/deactivate. */
    void save(ProductCategory category);

    /** Next sequential CAT-0001, CAT-0002, ... code, computed under the same file lock as save() uses. */
    String nextCategoryCode();
}
