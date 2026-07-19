package com.solardocs.infrastructure.pdf.util;

import java.math.BigDecimal;

public final class IndianCurrencyWordsConverter {

    private static final String[] ONES = {"", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
            "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"};
    private static final String[] TENS = {"", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"};

    private IndianCurrencyWordsConverter() {}

    public static String toWords(BigDecimal amount) {
        long rupees = amount.longValue();
        if (rupees == 0) return "Zero Indian Rupees";
        StringBuilder sb = new StringBuilder();

        long crore = rupees / 10000000; rupees %= 10000000;
        long lakh = rupees / 100000; rupees %= 100000;
        long thousand = rupees / 1000; rupees %= 1000;
        long hundred = rupees / 100; rupees %= 100;

        if (crore > 0) sb.append(twoDigit((int) crore)).append(" Crore ");
        if (lakh > 0) sb.append(twoDigit((int) lakh)).append(" Lakh ");
        if (thousand > 0) sb.append(twoDigit((int) thousand)).append(" Thousand ");
        if (hundred > 0) sb.append(ONES[(int) hundred]).append(" Hundred ");
        if (rupees > 0) {
            if (sb.length() > 0) sb.append("and ");
            sb.append(twoDigit((int) rupees));
        }
        return sb.toString().trim() + " Indian Rupees";
    }

    private static String twoDigit(int n) {
        if (n < 20) return ONES[n];
        return (TENS[n / 10] + " " + ONES[n % 10]).trim();
    }
}
