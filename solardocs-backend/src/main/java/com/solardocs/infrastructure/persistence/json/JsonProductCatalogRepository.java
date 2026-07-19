package com.solardocs.infrastructure.persistence.json;

import com.solardocs.application.ports.ProductCatalogRepository;
import com.solardocs.config.AppDataDirectoryConfig;
import com.solardocs.domain.vendor.Product;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * JSON-file-backed implementation of {@link ProductCatalogRepository}.
 * Reads/writes Config/product-catalog.json in the exact
 * {"products": [...]} shape the file already ships with, so any catalog
 * a vendor has already seeded (manually or via an earlier version)
 * loads without migration.
 */
@Repository
public class JsonProductCatalogRepository implements ProductCatalogRepository {

    /** Matches the on-disk {"products": [...]} envelope. */
    private record ProductCatalogFile(List<Product> products) {}

    private final AppDataDirectoryConfig dirs;
    private final JsonFileUtils json;

    public JsonProductCatalogRepository(AppDataDirectoryConfig dirs, JsonFileUtils json) {
        this.dirs = dirs;
        this.json = json;
    }

    private Path file() { return dirs.configDir().resolve("product-catalog.json"); }

    @Override
    public List<Product> findAll() {
        try {
            ProductCatalogFile catalog = json.read(file(), ProductCatalogFile.class);
            return (catalog == null || catalog.products() == null) ? List.of() : catalog.products();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read product catalog", e);
        }
    }

    @Override
    public void saveAll(List<Product> products) {
        try {
            json.writeAtomic(file(), new ProductCatalogFile(products));
        } catch (IOException e) {
            throw new RuntimeException("Failed to save product catalog", e);
        }
    }
}
