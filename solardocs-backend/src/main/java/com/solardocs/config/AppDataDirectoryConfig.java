package com.solardocs.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Component
public class AppDataDirectoryConfig {

    /**
     * Default PDF templates (registry + HTML files) bundled inside the
     * jar at src/main/resources/default-templates/. Copied into
     * Templates/ the first time a data directory doesn't already have
     * its own registry - this is what makes a brand-new data directory
     * (a first-run install, or a folder picked in the setup wizard)
     * immediately work with Generate Document instead of showing
     * "No templates available" until someone manually copies files in.
     */
    private static final String[] DEFAULT_TEMPLATE_FILES = {
            "templates-registry.json",
            "quotation-v1.html",
            "invoice-v1.html",
            "agreement-v1.html",
            "dcr-declaration-v1.html",
            "annexure1-v1.html",
            "net-meter-agreement-v1.html",
            "commissioning-report-v1.html",
            "work-completion-report-v1.html",
            "guarantee-certificate-v1.html"
    };

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
    public Path masterDataDir() { return root().resolve("MasterData"); }

    @PostConstruct
    public void ensureFolders() throws IOException {
        for (Path p : new Path[]{ root(), configDir(), indexesDir(), customersDir(),
                templatesDir(), reportsDir(), backupDir(), logsDir(), masterDataDir() }) {
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

        seedDefaultTemplatesIfMissing();
        seedDefaultProductCatalogIfMissing();
        seedDefaultProductCategoriesIfMissing();
    }

    /**
     * Copies the bundled default templates into Templates/ only if this
     * data directory doesn't already have its own templates-registry.json.
     * A vendor who has customized their templates is never overwritten -
     * this only fires on a genuinely empty/fresh Templates/ folder.
     */
    private void seedDefaultTemplatesIfMissing() throws IOException {
        Path registryFile = templatesDir().resolve("templates-registry.json");
        if (Files.exists(registryFile)) {
            return;
        }
        for (String fileName : DEFAULT_TEMPLATE_FILES) {
            copyClasspathResource("default-templates/" + fileName, templatesDir().resolve(fileName));
        }
    }

    /**
     * Same idea for the product catalog used to prefill quotation/invoice
     * line items - only seeded if Config/product-catalog.json doesn't
     * exist yet, so it never clobbers a catalog a vendor has already
     * edited via Settings.
     */
    private void seedDefaultProductCatalogIfMissing() throws IOException {
        Path catalogFile = configDir().resolve("product-catalog.json");
        if (Files.exists(catalogFile)) {
            return;
        }
        copyClasspathResource("default-config/product-catalog.json", catalogFile);
    }

    /**
     * Seeds a starter set of product categories (Solar Panel, Inverter,
     * Mounting Structure, ...) only if MasterData/categories.json doesn't
     * exist yet - same "never clobber an existing file" rule as the two
     * seed methods above, since a vendor may have already renamed or
     * deleted from the starter list via Settings.
     */
    private void seedDefaultProductCategoriesIfMissing() throws IOException {
        Path categoriesFile = masterDataDir().resolve("categories.json");
        if (Files.exists(categoriesFile)) {
            return;
        }
        copyClasspathResource("default-config/categories.json", categoriesFile);
    }

    private void copyClasspathResource(String classpathLocation, Path target) throws IOException {
        ClassPathResource resource = new ClassPathResource(classpathLocation);
        try (InputStream in = resource.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
