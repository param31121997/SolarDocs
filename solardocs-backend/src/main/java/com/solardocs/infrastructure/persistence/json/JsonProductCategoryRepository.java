package com.solardocs.infrastructure.persistence.json;

import com.solardocs.application.ports.ProductCategoryRepository;
import com.solardocs.config.AppDataDirectoryConfig;
import com.solardocs.domain.masterdata.ProductCategory;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JSON-file-backed implementation of {@link ProductCategoryRepository}.
 * Stores the whole category list as a single, plain JSON array at
 * MasterData/categories.json - no wrapper envelope - matching the exact
 * on-disk shape the module was specified with. Every mutation is a
 * read-modify-write of that one file, serialized through
 * {@link FileLockManager} the same way {@code JsonCustomerIndexRepository}
 * guards customers-index.json.
 */
@Repository
public class JsonProductCategoryRepository implements ProductCategoryRepository {

    private final AppDataDirectoryConfig dirs;
    private final JsonFileUtils json;
    private final FileLockManager locks;

    public JsonProductCategoryRepository(AppDataDirectoryConfig dirs, JsonFileUtils json, FileLockManager locks) {
        this.dirs = dirs;
        this.json = json;
        this.locks = locks;
    }

    private Path file() { return dirs.masterDataDir().resolve("categories.json"); }

    private List<ProductCategory> readAll() {
        try {
            ProductCategory[] stored = json.read(file(), ProductCategory[].class);
            return stored == null ? new ArrayList<>() : new ArrayList<>(List.of(stored));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read product categories", e);
        }
    }

    @Override
    public List<ProductCategory> findAll() {
        return List.copyOf(readAll());
    }

    @Override
    public Optional<ProductCategory> findById(String id) {
        return readAll().stream().filter(c -> c.id().equals(id)).findFirst();
    }

    @Override
    public Optional<ProductCategory> findByCategoryNameIgnoreCase(String categoryName) {
        return readAll().stream()
                .filter(c -> c.categoryName().equalsIgnoreCase(categoryName))
                .findFirst();
    }

    @Override
    public void save(ProductCategory category) {
        locks.withLock(file(), () -> {
            List<ProductCategory> current = readAll();
            current.removeIf(c -> c.id().equals(category.id()));
            current.add(category);
            try {
                json.writeAtomic(file(), current);
            } catch (IOException e) {
                throw new RuntimeException("Failed to save product category " + category.categoryCode(), e);
            }
            return null;
        });
    }

    @Override
    public String nextCategoryCode() {
        return locks.withLock(file(), () -> {
            int maxSequence = readAll().stream()
                    .map(ProductCategory::categoryCode)
                    .filter(code -> code != null && code.startsWith("CAT-"))
                    .mapToInt(code -> parseSequenceOrZero(code.substring(4)))
                    .max()
                    .orElse(0);
            return "CAT-%04d".formatted(maxSequence + 1);
        });
    }

    private int parseSequenceOrZero(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
