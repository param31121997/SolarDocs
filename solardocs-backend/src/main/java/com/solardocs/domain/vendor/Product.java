package com.solardocs.domain.vendor;

import java.math.BigDecimal;

/**
 * One entry in the vendor's product catalog - used to prefill quotation
 * / invoice line items so the vendor doesn't retype the same panel,
 * inverter, and BOS component pricing on every document. Persisted at
 * Config/product-catalog.json as {"products": [...]}, editable via the
 * Settings screen.
 */
public record Product(
        String code,
        String name,
        String type,
        String unit,
        BigDecimal defaultRate,
        String defaultGstPercent
) {}
