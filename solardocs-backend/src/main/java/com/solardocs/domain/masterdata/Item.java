package com.solardocs.domain.masterdata;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Master data - a reusable quotation line item (Solar PV Module, Inverter,
 * Module Mounting Structure, Cables (AC/DC) & MC4 Connectors, DCDB,
 * Earthing & Lightning Arrester Set, Designing Installation &
 * Commissioning, Transportation, Comprehensive Maintenance, ...).
 * Picked from the Generate Document screen's line-item table so a vendor
 * never has to retype the same item name, type, unit, rate and GST% on
 * every quotation/invoice - this is the single source of truth that
 * replaced the older Config/product-catalog.json prefill.
 * <p>
 * {@code type}, {@code unit}, {@code defaultRate} and {@code defaultGstPercent}
 * are optional defaults copied onto a line item when the item is picked;
 * the vendor can still edit any of them per-document afterwards.
 * <p>
 * Deletion is soft: {@code active=false} rather than removal, since an
 * item already referenced by an existing quotation must not disappear
 * from history. Persisted as a plain JSON array at MasterData/items.json.
 */
public record Item(
        String id,
        String itemName,
        String description,
        String type,
        String unit,
        BigDecimal defaultRate,
        String defaultGstPercent,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {

    public static Item create(String id, String itemName, String description, String type,
                               String unit, BigDecimal defaultRate, String defaultGstPercent) {
        Instant now = Instant.now();
        return new Item(id, itemName, description, type, unit, defaultRate, defaultGstPercent, true, now, now);
    }

    public Item withDetails(String itemName, String description, String type,
                             String unit, BigDecimal defaultRate, String defaultGstPercent) {
        return new Item(id, itemName, description, type, unit, defaultRate, defaultGstPercent,
                active, createdAt, Instant.now());
    }

    public Item withActive(boolean active) {
        return new Item(id, itemName, description, type, unit, defaultRate, defaultGstPercent,
                active, createdAt, Instant.now());
    }
}
