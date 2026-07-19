package com.solardocs.api.settings;

import com.solardocs.api.common.ApiResponse;
import com.solardocs.application.ports.VendorProfileRepository;
import com.solardocs.domain.vendor.VendorProfile;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings/vendor-profile")
public class VendorProfileController {

    private final VendorProfileRepository repository;

    public VendorProfileController(VendorProfileRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public ApiResponse<VendorProfile> get() {
        return ApiResponse.ok(repository.find().orElse(null));
    }

    @PutMapping
    public ApiResponse<VendorProfile> update(@RequestBody VendorProfile profile) {
        repository.save(profile);
        return ApiResponse.ok(profile);
    }
}
