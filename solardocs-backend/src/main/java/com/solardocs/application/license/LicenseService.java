package com.solardocs.application.license;

import com.solardocs.config.AppDataDirectoryConfig;
import com.solardocs.domain.license.License;
import com.solardocs.infrastructure.licensing.LicenseKeyValidator;
import com.solardocs.infrastructure.licensing.MachineFingerprintProvider;
import com.solardocs.infrastructure.persistence.json.JsonFileUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;

@Service
public class LicenseService {

    private final AppDataDirectoryConfig dirs;
    private final JsonFileUtils json;
    private final LicenseKeyValidator validator;
    private final MachineFingerprintProvider fingerprintProvider;

    public LicenseService(AppDataDirectoryConfig dirs, JsonFileUtils json,
                           LicenseKeyValidator validator, MachineFingerprintProvider fingerprintProvider) {
        this.dirs = dirs;
        this.json = json;
        this.validator = validator;
        this.fingerprintProvider = fingerprintProvider;
    }

    private java.nio.file.Path file() { return dirs.configDir().resolve("license.json"); }

    public License activate(String licenseKey) {
        String fingerprint = fingerprintProvider.fingerprint();
        if (!validator.isValid(licenseKey, fingerprint)) {
            throw new IllegalArgumentException("Invalid or expired license key for this machine");
        }
        License license = new License(licenseKey, fingerprint, Instant.now().toString(), "ACTIVE");
        try {
            json.writeAtomic(file(), license);
        } catch (IOException e) { throw new RuntimeException(e); }
        return license;
    }

    public License status() {
        try {
            License stored = json.read(file(), License.class);
            if (stored == null) return new License(null, fingerprintProvider.fingerprint(), null, "NOT_ACTIVATED");
            boolean stillValid = validator.isValid(stored.key(), fingerprintProvider.fingerprint());
            return new License(stored.key(), stored.machineFingerprint(), stored.activatedAt(),
                    stillValid ? "ACTIVE" : "EXPIRED_OR_INVALID");
        } catch (IOException e) { throw new RuntimeException(e); }
    }
}
