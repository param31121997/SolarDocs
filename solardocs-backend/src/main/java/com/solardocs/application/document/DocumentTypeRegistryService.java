package com.solardocs.application.document;

import com.solardocs.config.AppDataDirectoryConfig;
import com.solardocs.infrastructure.persistence.json.JsonFileUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class DocumentTypeRegistryService {

    public record DocumentTypeEntry(String code, String label, String category) {}
    private record Registry(List<DocumentTypeEntry> documentTypes) {}

    private final AppDataDirectoryConfig dirs;
    private final JsonFileUtils json;

    public DocumentTypeRegistryService(AppDataDirectoryConfig dirs, JsonFileUtils json) {
        this.dirs = dirs;
        this.json = json;
    }

    public List<DocumentTypeEntry> listAll() {
        try {
            Registry r = json.read(dirs.configDir().resolve("document-types.json"), Registry.class);
            return r == null ? List.of() : r.documentTypes();
        } catch (IOException e) { throw new RuntimeException(e); }
    }

    public void addType(DocumentTypeEntry entry) {
        try {
            var current = new java.util.ArrayList<>(listAll());
            current.add(entry);
            json.writeAtomic(dirs.configDir().resolve("document-types.json"), Map.of("documentTypes", current));
        } catch (IOException e) { throw new RuntimeException(e); }
    }
}
