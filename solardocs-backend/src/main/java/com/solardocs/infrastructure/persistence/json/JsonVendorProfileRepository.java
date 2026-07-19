package com.solardocs.infrastructure.persistence.json;

import com.solardocs.application.ports.VendorProfileRepository;
import com.solardocs.config.AppDataDirectoryConfig;
import com.solardocs.domain.vendor.VendorProfile;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.Optional;

@Repository
public class JsonVendorProfileRepository implements VendorProfileRepository {

    private final AppDataDirectoryConfig dirs;
    private final JsonFileUtils json;

    public JsonVendorProfileRepository(AppDataDirectoryConfig dirs, JsonFileUtils json) {
        this.dirs = dirs;
        this.json = json;
    }

    private java.nio.file.Path file() { return dirs.configDir().resolve("vendor-profile.json"); }

    @Override
    public Optional<VendorProfile> find() {
        try {
            return Optional.ofNullable(json.read(file(), VendorProfile.class));
        } catch (IOException e) { throw new RuntimeException(e); }
    }

    @Override
    public void save(VendorProfile profile) {
        try { json.writeAtomic(file(), profile); } catch (IOException e) { throw new RuntimeException(e); }
    }
}
