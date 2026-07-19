package com.solardocs.api.dashboard;

import com.solardocs.api.common.ApiResponse;
import com.solardocs.application.dashboard.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/api/dashboard/summary")
    public ApiResponse<DashboardService.DashboardSummary> summary() {
        return ApiResponse.ok(dashboardService.summary());
    }
}
