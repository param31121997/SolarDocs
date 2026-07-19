package com.solardocs;

import com.solardocs.config.DataDirectoryBootstrap;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SolardocsBackendApplication {
    public static void main(String[] args) {
        // If a first-run data directory choice was saved previously, apply
        // it before the Spring context is created - AppDataDirectoryConfig
        // resolves ${SOLARDOCS_DATA_DIR:./solardocs-data} at bean-creation
        // time, so the system property must be set before SpringApplication.run().
        DataDirectoryBootstrap.readStoredPath()
                .ifPresent(path -> System.setProperty("SOLARDOCS_DATA_DIR", path));

        SpringApplication.run(SolardocsBackendApplication.class, args);
    }
}
