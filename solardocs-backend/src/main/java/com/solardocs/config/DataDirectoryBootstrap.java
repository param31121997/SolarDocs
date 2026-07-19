package com.solardocs.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

/**
 * Remembers which folder SolarDocs should store its data in, across
 * restarts, independent of the data directory itself (obviously the
 * data directory can't tell you where the data directory is). The
 * chosen path is written to a small properties file in a fixed,
 * OS-level location - the user's home folder - which is read in
 * {@code main()}, before the Spring application context (and therefore
 * before {@link AppDataDirectoryConfig}) is created, by setting the
 * SOLARDOCS_DATA_DIR system property so the existing
 * {@code ${SOLARDOCS_DATA_DIR:./solardocs-data}} placeholder in
 * application.properties picks it up exactly as if it had been passed
 * as an environment variable.
 * <p>
 * If no first-run choice has ever been saved, this resolves to nothing
 * and the app falls back to its existing default behavior - existing
 * installs that never see the setup wizard are completely unaffected.
 */
public final class DataDirectoryBootstrap {

    private static final Path BOOTSTRAP_FILE =
            Path.of(System.getProperty("user.home"), ".solardocs", "bootstrap.properties");
    private static final String KEY = "dataDir";

    private DataDirectoryBootstrap() {
    }

    public static Optional<String> readStoredPath() {
        if (Files.notExists(BOOTSTRAP_FILE)) {
            return Optional.empty();
        }
        try {
            Properties props = new Properties();
            try (var in = Files.newInputStream(BOOTSTRAP_FILE)) {
                props.load(in);
            }
            String value = props.getProperty(KEY);
            return (value == null || value.isBlank()) ? Optional.empty() : Optional.of(value);
        } catch (IOException e) {
            // A corrupt bootstrap file should never prevent the app from
            // starting - just fall back to the default data directory.
            return Optional.empty();
        }
    }

    public static void writeStoredPath(String path) {
        try {
            Files.createDirectories(BOOTSTRAP_FILE.getParent());
            Properties props = new Properties();
            props.setProperty(KEY, path);
            try (var out = Files.newOutputStream(BOOTSTRAP_FILE)) {
                props.store(out, "SolarDocs data directory location - edit only while the app is stopped");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save data directory setting to " + BOOTSTRAP_FILE, e);
        }
    }

    public static boolean isConfigured() {
        return readStoredPath().isPresent();
    }
}
