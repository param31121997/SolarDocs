package com.solardocs.infrastructure.pdf.strategy;

import com.solardocs.domain.common.QuotationLineItem;
import com.solardocs.domain.customer.Customer;
import com.solardocs.infrastructure.pdf.util.IndianCurrencyWordsConverter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component
public class QuotationGenerationStrategy implements DocumentGenerationStrategy {

    @Override
    public String templateCode() { return "QUOTATION"; }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> buildModel(Customer customer, Map<String, Object> extraFields) {
        String billNo = (String) extraFields.getOrDefault("billNo", "GNEC-" + System.currentTimeMillis() % 10000);
        List<Map<String, Object>> rawLines = (List<Map<String, Object>>) extraFields.getOrDefault("lineItems", List.of());

        List<QuotationLineItem> lineItems = rawLines.stream().map(m -> new QuotationLineItem(
                ((Number) m.get("slNo")).intValue(),
                (String) m.get("item"),
                (String) m.get("type"),
                String.valueOf(m.getOrDefault("qty", "")),
                (String) m.getOrDefault("unit", ""),
                new BigDecimal(String.valueOf(m.getOrDefault("rate", "0"))),
                (String) m.getOrDefault("gstPercent", ""),
                new BigDecimal(String.valueOf(m.getOrDefault("amount", "0")))
        )).toList();

        BigDecimal total = lineItems.stream().map(QuotationLineItem::amount).reduce(BigDecimal.ZERO, BigDecimal::add);

        return Map.of(
                "billNo", billNo,
                "date", LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                "customerName", customer.getName(),
                "address", customer.getAddress(),
                "mobile", customer.getMobile(),
                "state", customer.getAddress() != null ? customer.getAddress().state() : "",
                "lineItems", lineItems,
                "total", total,
                "amountInWords", IndianCurrencyWordsConverter.toWords(total),
                "bankDetails", extraFields.getOrDefault("bankDetails", Map.of())
        );
    }
}
