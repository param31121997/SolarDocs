package com.solardocs.application.dashboard;

import com.solardocs.application.ports.CustomerIndexRepository;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    public record DashboardSummary(
            int totalCustomers,
            Map<String, Long> countsByStatus,
            long pendingDocuments,
            long installedThisMonth
    ) {}

    private final CustomerIndexRepository indexRepository;

    public DashboardService(CustomerIndexRepository indexRepository) {
        this.indexRepository = indexRepository;
    }

    public DashboardSummary summary() {
        var all = indexRepository.findAll();
        Map<String, Long> byStatus = all.stream()
                .collect(Collectors.groupingBy(CustomerIndexRepository.IndexEntry::status, Collectors.counting()));

        long pendingDocs = byStatus.getOrDefault("LEAD", 0L) + byStatus.getOrDefault("QUOTATION_SENT", 0L);
        long installedThisMonth = byStatus.getOrDefault("COMMISSIONED", 0L);

        return new DashboardSummary(all.size(), byStatus, pendingDocs, installedThisMonth);
    }
}
