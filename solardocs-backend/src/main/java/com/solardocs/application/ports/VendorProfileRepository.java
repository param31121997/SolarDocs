package com.solardocs.application.ports;

import com.solardocs.domain.vendor.VendorProfile;
import java.util.Optional;

public interface VendorProfileRepository {
    Optional<VendorProfile> find();
    void save(VendorProfile profile);
}
