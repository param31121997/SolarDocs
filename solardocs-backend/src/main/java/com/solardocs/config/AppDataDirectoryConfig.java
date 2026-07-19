package com.solardocs.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class AppDataDirectoryConfig {

    private final String dataDir;

    public AppDataDirectoryConfig(@Value("${solardocs.data-dir}") String dataDir) {
        this.dataDir = dataDir;
    }

    public Path root() { return Path.of(dataDir); }
    public Path configDir() { return root().resolve("Config"); }
    public Path indexesDir() { return root().resolve("Indexes"); }
    public Path customersDir() { return root().resolve("Customers"); }
    public Path templatesDir() { return root().resolve("Templates"); }
    public Path reportsDir() { return root().resolve("Reports"); }
    public Path backupDir() { return root().resolve("Backup"); }
    public Path logsDir() { return root().resolve("Logs"); }

    @PostConstruct
    public void ensureFolders() throws IOException {
        for (Path p : new Path[]{ root(), configDir(), indexesDir(), customersDir(),
                templatesDir(), reportsDir(), backupDir(), logsDir() }) {
            Files.createDirectories(p);
        }
        Path configFile = configDir().resolve("config.json");
        if (Files.notExists(configFile)) {
            String initial = """
                {
                  "appVersion": "1.0.0",
                  "dataDirVersion": 1,
                  "nextCustomerNumber": 1,
                  "vendorId": "VEND-0001",
                  "dataDirectory": "%s"
                }
                """.formatted(dataDir.replace("\\", "\\\\"));
            Files.writeString(configFile, initial);
        }
        Path indexFile = indexesDir().resolve("customers-index.json");
        if (Files.notExists(indexFile)) {
            Files.writeString(indexFile, "{\"lastUpdated\": null, \"customers\": []}");
        }
    }
}
