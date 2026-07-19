package com.solardocs.api.reports;

import com.solardocs.application.reports.CustomerReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReportsController {

    private final CustomerReportService reportService;

    public ReportsController(CustomerReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/api/reports/customers")
    public ResponseEntity<byte[]> export(@RequestParam(defaultValue = "csv") String format) {
        byte[] body = reportService.exportCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=customers.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(body);
    }
}
