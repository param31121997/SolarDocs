package com.solardocs.domain.common;

import java.math.BigDecimal;

public record QuotationLineItem(
        int slNo, String item, String type, String qty, String unit,
        BigDecimal rate, String gstPercent, BigDecimal amount
) {}
