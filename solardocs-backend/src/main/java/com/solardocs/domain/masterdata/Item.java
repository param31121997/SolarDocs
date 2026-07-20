package com.solardocs.domain.masterdata;

import java.time.Instant;

/**
 * Master data - a reusable quotation line item (Solar PV Module, Inverter,
 * Module Mounting Structure, Cables (AC/DC) & MC4 Connectors, DCDB,
 * Earthing & Lightning Arrester Set, Designing Installation &
 * Commissioning, Transportation, Comprehensive Maintenance, ...).
 * Picked from the catalog when building a quotation's line items,
 * so a vendor never has to retype the same item name/description twice.
 * <p>
 * Deletion is soft: {@code active=false} rather than removal, since an
 * item already referenced by an existing quotation must not disappear
 * from history. Persisted as a plain JSON array at MasterData/items.json.
 */
public record Item(
        String id,
        String itemName,
        String description,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {

    public static Item create(String id, String itemName, String description) {
        Instant now = Instant.now();
        return new Item(id, itemName, description, true, now, now);
    }

    public Item withDetails(String itemName, String description) {
        return new Item(id, itemName, description, active, createdAt, Instant.now());
    }

    public Item withActive(boolean active) {
        return new Item(id, itemName, description, active, createdAt, Instant.now());
    }
}
