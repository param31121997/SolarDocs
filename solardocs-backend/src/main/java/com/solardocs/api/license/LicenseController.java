package com.solardocs.api.license;

import com.solardocs.api.common.ApiResponse;
import com.solardocs.application.license.LicenseService;
import com.solardocs.domain.license.License;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/license")
public class LicenseController {

    private final LicenseService licenseService;

    public LicenseController(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    public record ActivateRequest(String licenseKey) {}

    @PostMapping("/activate")
    public ApiResponse<License> activate(@RequestBody ActivateRequest req) {
        return ApiResponse.ok(licenseService.activate(req.licenseKey()));
    }

    @GetMapping("/status")
    public ApiResponse<License> status() {
        return ApiResponse.ok(licenseService.status());
    }
}
