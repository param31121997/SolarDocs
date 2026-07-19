package com.solardocs.domain.masterdata;

import java.time.Instant;

/**
 * Master data - a category products are grouped under (Solar Panel,
 * Inverter, Mounting Structure, Cable, ...). Reused wherever a product
 * needs to be classified: today only Product Catalog line items, later
 * the Products, Specifications, Quotation and Inventory modules
 * described in the module brief - none of which need this record's
 * shape to change, only new relationships pointing at {@link #id()}.
 * <p>
 * Deletion is soft: {@code active=false} rather than removal, since a
 * category already referenced elsewhere must not disappear from
 * history. Persisted as a plain JSON array at MasterData/categories.json.
 */
public record ProductCategory(
        String id,
        String categoryCode,
        String categoryName,
        String description,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {

    public static ProductCategory create(String id, String categoryCode, String categoryName, String description) {
        Instant now = Instant.now();
        return new ProductCategory(id, categoryCode, categoryName, description, true, now, now);
    }

    public ProductCategory withDetails(String categoryName, String description) {
        return new ProductCategory(id, categoryCode, categoryName, description, active, createdAt, Instant.now());
    }

    public ProductCategory withActive(boolean active) {
        return new ProductCategory(id, categoryCode, categoryName, description, active, createdAt, Instant.now());
    }
}
