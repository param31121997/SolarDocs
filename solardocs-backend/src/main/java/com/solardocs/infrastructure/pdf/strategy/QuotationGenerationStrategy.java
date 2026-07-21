package com.solardocs.infrastructure.pdf.strategy;

import com.solardocs.domain.common.Address;
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
                toBigDecimal(m.get("rate")),
                (String) m.getOrDefault("gstPercent", ""),
                toBigDecimal(m.get("amount"))
        )).toList();

        BigDecimal total = lineItems.stream().map(QuotationLineItem::amount).reduce(BigDecimal.ZERO, BigDecimal::add);

        // customer.getAddress() can be null for a customer created without full
        // details filled in yet - the template dereferences address.addressLine/
        // village/pincode directly, so a null here previously crashed PDF
        // rendering with a SpringEL "cannot be found on null" error. Default to
        // an all-blank Address instead so the quotation still generates, just
        // with blank address fields the vendor can fill in on the printout.
        Address address = customer.getAddress() != null ? customer.getAddress() : new Address("", "", "", "", "");

        return Map.of(
                "billNo", billNo,
                "date", LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                "customerName", customer.getName(),
                "address", address,
                "mobile", customer.getMobile(),
                "state", address.state() != null ? address.state() : "",
                "lineItems", lineItems,
                "total", total,
                "amountInWords", IndianCurrencyWordsConverter.toWords(total),
                "bankDetails", extraFields.getOrDefault("bankDetails", Map.of())
        );
    }

    /** A vendor leaving Rate/Amount blank on a row sends "" (or the key is absent) rather than "0" - both must fall back to zero instead of blowing up BigDecimal's parser. */
    private static BigDecimal toBigDecimal(Object value) {
        String s = value == null ? "" : String.valueOf(value).trim();
        if (s.isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(s);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}
