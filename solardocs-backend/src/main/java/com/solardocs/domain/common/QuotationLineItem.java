package com.solardocs.domain.common;

import java.math.BigDecimal;

/**
 * A single row on a Quotation/Invoice.
 * <p>
 * {@code rate}/{@code amount} hold the raw numeric values (used for totals),
 * while {@code rateDisplay}/{@code gstDisplay}/{@code amountDisplay} hold the
 * pre-formatted strings templates should actually print (Indian digit
 * grouping, a trailing "%" on GST, and "Included" instead of "0" for
 * flat-rate items like Transportation / Comprehensive Maintenance that have
 * no unit and no charge of their own).
 */
public record QuotationLineItem(
        int slNo, String item, String type, String qty, String unit,
        BigDecimal rate, String gstPercent, BigDecimal amount,
        String rateDisplay, String gstDisplay, String amountDisplay
) {}
