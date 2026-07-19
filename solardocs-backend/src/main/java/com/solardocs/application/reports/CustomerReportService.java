package com.solardocs.application.reports;

import com.solardocs.application.ports.CustomerIndexRepository;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

@Service
public class CustomerReportService {

    private final CustomerIndexRepository indexRepository;

    public CustomerReportService(CustomerIndexRepository indexRepository) {
        this.indexRepository = indexRepository;
    }

    public byte[] exportCsv() {
        var customers = indexRepository.findAll();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(out, true, StandardCharsets.UTF_8)) {
            writer.println("Customer ID,Name,Mobile,Status,Village,Consumer Number,Application Number");
            for (var c : customers) {
                writer.printf("%s,%s,%s,%s,%s,%s,%s%n",
                        c.customerId(), escape(c.name()), c.mobile(), c.status(),
                        escape(c.village()), safe(c.consumerNumber()), safe(c.applicationNumber()));
            }
        }
        return out.toByteArray();
    }

    private String escape(String s) { return s == null ? "" : s.replace(",", " "); }
    private String safe(String s) { return s == null ? "" : s; }
}
