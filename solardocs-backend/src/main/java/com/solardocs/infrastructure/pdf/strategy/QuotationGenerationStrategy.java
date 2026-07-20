package com.solardocs.infrastructure.pdf.strategy;

import com.solardocs.application.ports.VendorProfileRepository;
import com.solardocs.domain.common.Address;
import com.solardocs.domain.common.QuotationLineItem;
import com.solardocs.domain.customer.Customer;
import com.solardocs.domain.vendor.VendorProfile;
import com.solardocs.infrastructure.pdf.util.IndianCurrencyWordsConverter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component
public class QuotationGenerationStrategy implements DocumentGenerationStrategy {

    private final VendorProfileRepository vendorProfileRepository;

    public QuotationGenerationStrategy(VendorProfileRepository vendorProfileRepository) {
        this.vendorProfileRepository = vendorProfileRepository;
    }

    @Override
    public String templateCode() { return "QUOTATION"; }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> buildModel(Customer customer, Map<String, Object> extraFields) {
        String billNo = (String) extraFields.getOrDefault("billNo", "GNEC-" + System.currentTimeMillis() % 10000);
        List<Map<String, Object>> rawLines = (List<Map<String, Object>>) extraFields.getOrDefault("lineItems", List.of());

        List<QuotationLineItem> lineItems = rawLines.stream().map(this::toLineItem).toList();

        BigDecimal total = lineItems.stream().map(QuotationLineItem::amount).reduce(BigDecimal.ZERO, BigDecimal::add);

        // customer.getAddress() can be null for a customer created without full
        // details filled in yet - the template dereferences address.addressLine/
        // village/pincode directly, so a null here previously crashed PDF
        // rendering with a SpringEL "cannot be found on null" error. Default to
        // an all-blank Address instead so the quotation still generates, just
        // with blank address fields the vendor can fill in on the printout.
        Address address = customer.getAddress() != null ? customer.getAddress() : new Address("", "", "", "", "");

        // Vendor's own company details (letterhead, GSTIN, bank account, signatory)
        // live in Config/vendor-profile.json, set once in Settings - not something
        // the vendor should have to retype into extraFields on every document.
        // Fall back to whatever extraFields provides (or blanks) if no profile has
        // been saved yet, so document generation never crashes on a fresh install.
        VendorProfile vendor = vendorProfileRepository.find().orElse(null);
        String companyName = vendor != null ? nullToEmpty(vendor.companyName()) : (String) extraFields.getOrDefault("vendorCompanyName", "");
        String companyGstin = vendor != null ? nullToEmpty(vendor.gstin()) : "";
        String companyAddress = vendor != null ? nullToEmpty(vendor.registeredAddress()) : (String) extraFields.getOrDefault("vendorRegisteredAddress", "");
        String signatoryName = vendor != null ? nullToEmpty(vendor.signatoryName()) : "";

        Map<String, Object> bankDetails;
        if (vendor != null && (notEmpty(vendor.bankAccountName()) || notEmpty(vendor.bankAccountNumber()) || notEmpty(vendor.bankIfsc()))) {
            bankDetails = Map.of(
                    "accountName", nullToEmpty(vendor.bankAccountName()),
                    "accountNumber", nullToEmpty(vendor.bankAccountNumber()),
                    "ifsc", nullToEmpty(vendor.bankIfsc())
            );
        } else {
            @SuppressWarnings("unchecked")
            Map<String, Object> fromExtra = (Map<String, Object>) extraFields.getOrDefault("bankDetails", Map.of());
            bankDetails = fromExtra;
        }

        // Map.of() tops out at 10 key/value pairs - we need well over that, so
        // build with Map.ofEntries() instead (still an immutable map;
        // InvoiceGenerationStrategy wraps this in a mutable HashMap before
        // adding its own receipt fields).
        return Map.ofEntries(
                Map.entry("billNo", billNo),
                Map.entry("date", LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))),
                Map.entry("companyName", companyName),
                Map.entry("companyGstin", companyGstin),
                Map.entry("companyAddress", companyAddress),
                Map.entry("signatoryName", signatoryName),
                Map.entry("customerName", customer.getName()),
                Map.entry("address", address),
                Map.entry("mobile", customer.getMobile()),
                Map.entry("state", address.state() != null ? address.state() : ""),
                Map.entry("lineItems", lineItems),
                Map.entry("total", total),
                Map.entry("totalDisplay", formatIndianAmount(total)),
                Map.entry("subTotal", total),
                Map.entry("subTotalDisplay", formatIndianAmount(total)),
                Map.entry("amountInWords", IndianCurrencyWordsConverter.toWords(total)),
                Map.entry("bankDetails", bankDetails)
        );
    }

    private static String nullToEmpty(String s) { return s == null ? "" : s; }
    private static boolean notEmpty(String s) { return s != null && !s.isBlank(); }

    /** Builds one line item plus its ready-to-print display strings. */
    private QuotationLineItem toLineItem(Map<String, Object> m) {
        String unit = (String) m.getOrDefault("unit", "");
        String gstPercent = (String) m.getOrDefault("gstPercent", "");
        BigDecimal rate = toBigDecimal(m.get("rate"));
        BigDecimal amount = toBigDecimal(m.get("amount"));

        // A row with no unit and no rate is a flat/lump-sum item the vendor
        // hasn't priced separately (e.g. Transportation, Comprehensive
        // Maintenance bundled into the job) - show "Included" instead of a
        // misleading "0", matching how these are written on paper quotations.
        boolean flatIncluded = (unit == null || unit.isBlank()) && rate.compareTo(BigDecimal.ZERO) == 0;

        String rateDisplay = flatIncluded ? "Included" : formatIndianAmount(rate);
        String amountDisplay = flatIncluded ? "Included" : formatIndianAmount(amount);
        String gstDisplay = (gstPercent == null || gstPercent.isBlank()) ? "" : gstPercent + "%";

        return new QuotationLineItem(
                ((Number) m.get("slNo")).intValue(),
                (String) m.get("item"),
                (String) m.get("type"),
                String.valueOf(m.getOrDefault("qty", "")),
                unit,
                rate,
                gstPercent,
                amount,
                rateDisplay,
                gstDisplay,
                amountDisplay
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

    /**
     * Formats a rupee amount with Indian digit grouping (e.g. 153090 -&gt;
     * "1,53,090"). Whole-rupee amounts are printed without decimals; anything
     * with paise keeps 2 decimal places.
     */
    static String formatIndianAmount(BigDecimal value) {
        BigDecimal v = value == null ? BigDecimal.ZERO : value;
        boolean whole = v.stripTrailingZeros().scale() <= 0;
        BigDecimal rounded = v.setScale(whole ? 0 : 2, RoundingMode.HALF_UP);

        boolean negative = rounded.signum() < 0;
        String plain = rounded.abs().toPlainString();
        String integerPart = plain;
        String fractionPart = "";
        int dot = plain.indexOf('.');
        if (dot >= 0) {
            integerPart = plain.substring(0, dot);
            fractionPart = plain.substring(dot);
        }

        // Indian grouping: last 3 digits together, then groups of 2 before that.
        String lastThree = integerPart.length() > 3 ? integerPart.substring(integerPart.length() - 3) : integerPart;
        String remaining = integerPart.length() > 3 ? integerPart.substring(0, integerPart.length() - 3) : "";
        StringBuilder grouped = new StringBuilder();
        for (int i = 0; i < remaining.length(); i++) {
            if (i > 0 && (remaining.length() - i) % 2 == 0) {
                grouped.append(',');
            }
            grouped.append(remaining.charAt(i));
        }
        String result = (grouped.length() > 0 ? grouped + "," : "") + lastThree + fractionPart;
        return (negative ? "-" : "") + result;
    }
}
