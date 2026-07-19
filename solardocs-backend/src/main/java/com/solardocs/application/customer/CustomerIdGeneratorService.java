package com.solardocs.application.customer;

import com.solardocs.config.AppDataDirectoryConfig;
import com.solardocs.domain.customer.CustomerId;
import com.solardocs.infrastructure.persistence.json.FileLockManager;
import com.solardocs.infrastructure.persistence.json.JsonFileUtils;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.Map;

@Service
public class CustomerIdGeneratorService {

    private final AppDataDirectoryConfig dirs;
    private final JsonFileUtils json;
    private final FileLockManager locks;

    public CustomerIdGeneratorService(AppDataDirectoryConfig dirs, JsonFileUtils json, FileLockManager locks) {
        this.dirs = dirs;
        this.json = json;
        this.locks = locks;
    }

    @SuppressWarnings("unchecked")
    public CustomerId nextId() {
        Path configFile = dirs.configDir().resolve("config.json");
        return locks.withLock(configFile, () -> {
            Map<String, Object> config = json.read(configFile, Map.class);
            int next = (int) config.getOrDefault("nextCustomerNumber", 1);
            config.put("nextCustomerNumber", next + 1);
            json.writeAtomic(configFile, config);
            return CustomerId.of(next);
        });
    }
}
