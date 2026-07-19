package com.solardocs.infrastructure.persistence.json;

import com.solardocs.application.ports.CustomerIndexRepository;
import com.solardocs.config.AppDataDirectoryConfig;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Repository
public class JsonCustomerIndexRepository implements CustomerIndexRepository {

    private record IndexFile(String lastUpdated, List<IndexEntry> customers) {}

    private final AppDataDirectoryConfig dirs;
    private final JsonFileUtils json;
    private final FileLockManager locks;

    public JsonCustomerIndexRepository(AppDataDirectoryConfig dirs, JsonFileUtils json, FileLockManager locks) {
        this.dirs = dirs;
        this.json = json;
        this.locks = locks;
    }

    private Path indexFile() { return dirs.indexesDir().resolve("customers-index.json"); }

    @Override
    public List<IndexEntry> findAll() {
        try {
            IndexFile f = json.read(indexFile(), IndexFile.class);
            return f == null || f.customers() == null ? List.of() : f.customers();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<IndexEntry> search(String q, String status, String village, String district) {
        return findAll().stream()
                .filter(e -> q == null || q.isBlank()
                        || e.name().toLowerCase().contains(q.toLowerCase())
                        || e.customerId().toLowerCase().contains(q.toLowerCase())
                        || (e.mobile() != null && e.mobile().contains(q))
                        || (e.consumerNumber() != null && e.consumerNumber().toLowerCase().contains(q.toLowerCase()))
                        || (e.applicationNumber() != null && e.applicationNumber().toLowerCase().contains(q.toLowerCase())))
                .filter(e -> status == null || status.isBlank() || status.equalsIgnoreCase(e.status()))
                .filter(e -> village == null || village.isBlank() || village.equalsIgnoreCase(e.village()))
                .filter(e -> district == null || district.isBlank() || district.equalsIgnoreCase(e.district()))
                .toList();
    }

    @Override
    public void upsert(IndexEntry entry) {
        locks.withLock(indexFile(), () -> {
            List<IndexEntry> current = new ArrayList<>(findAll());
            current.removeIf(e -> e.customerId().equals(entry.customerId()));
            current.add(entry);
            try {
                json.writeAtomic(indexFile(), new IndexFile(Instant.now().toString(), current));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    @Override
    public void rebuildFrom(List<IndexEntry> entries) {
        locks.withLock(indexFile(), () -> {
            try {
                json.writeAtomic(indexFile(), new IndexFile(Instant.now().toString(), entries));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }
}
