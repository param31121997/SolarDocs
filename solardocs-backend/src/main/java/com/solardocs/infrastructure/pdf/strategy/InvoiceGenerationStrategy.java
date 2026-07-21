package com.solardocs.infrastructure.pdf.strategy;

import com.solardocs.domain.customer.Customer;
import com.solardocs.infrastructure.pdf.util.IndianCurrencyWordsConverter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
public class InvoiceGenerationStrategy implements DocumentGenerationStrategy {

    private final QuotationGenerationStrategy quotationStrategy;

    // Invoice re-uses the exact same line-item building logic as Quotation — composition over duplication.
    public InvoiceGenerationStrategy(QuotationGenerationStrategy quotationStrategy) {
        this.quotationStrategy = quotationStrategy;
    }

    @Override
    public String templateCode() { return "INVOICE"; }

    @Override
    public Map<String, Object> buildModel(Customer customer, Map<String, Object> extraFields) {
        Map<String, Object> base = new java.util.HashMap<>(quotationStrategy.buildModel(customer, extraFields));

        BigDecimal received = new BigDecimal(String.valueOf(extraFields.getOrDefault("amountReceived", "0")));
        base.put("receiptNo", extraFields.getOrDefault("receiptNo", "GNEC-" + (System.currentTimeMillis() % 10000 + 1)));
        base.put("receiptDate", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        base.put("amountReceived", received);
        base.put("amountReceivedInWords", IndianCurrencyWordsConverter.toWords(received));
        return base;
    }
}
