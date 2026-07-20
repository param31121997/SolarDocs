package com.solardocs.infrastructure.persistence.json;

import com.solardocs.application.ports.ItemRepository;
import com.solardocs.config.AppDataDirectoryConfig;
import com.solardocs.domain.masterdata.Item;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JSON-file-backed implementation of {@link ItemRepository}.
 * Stores the whole item list as a single, plain JSON array at
 * MasterData/items.json - no wrapper envelope - matching the exact
 * on-disk shape the module was specified with. Every mutation is a
 * read-modify-write of that one file, serialized through
 * {@link FileLockManager} the same way {@code JsonProductCategoryRepository}
 * guards categories.json.
 */
@Repository
public class JsonItemRepository implements ItemRepository {

    private final AppDataDirectoryConfig dirs;
    private final JsonFileUtils json;
    private final FileLockManager locks;

    public JsonItemRepository(AppDataDirectoryConfig dirs, JsonFileUtils json, FileLockManager locks) {
        this.dirs = dirs;
        this.json = json;
        this.locks = locks;
    }

    private Path file() { return dirs.masterDataDir().resolve("items.json"); }

    private List<Item> readAll() {
        try {
            Item[] stored = json.read(file(), Item[].class);
            return stored == null ? new ArrayList<>() : new ArrayList<>(List.of(stored));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read items", e);
        }
    }

    @Override
    public List<Item> findAll() {
        return List.copyOf(readAll());
    }

    @Override
    public Optional<Item> findById(String id) {
        return readAll().stream().filter(i -> i.id().equals(id)).findFirst();
    }

    @Override
    public Optional<Item> findByItemNameIgnoreCase(String itemName) {
        return readAll().stream()
                .filter(i -> i.itemName().equalsIgnoreCase(itemName))
                .findFirst();
    }

    @Override
    public void save(Item item) {
        locks.withLock(file(), () -> {
            List<Item> current = readAll();
            current.removeIf(i -> i.id().equals(item.id()));
            current.add(item);
            try {
                json.writeAtomic(file(), current);
            } catch (IOException e) {
                throw new RuntimeException("Failed to save item " + item.itemName(), e);
            }
            return null;
        });
    }
}
