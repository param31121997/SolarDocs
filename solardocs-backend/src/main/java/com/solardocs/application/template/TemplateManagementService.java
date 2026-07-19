package com.solardocs.application.template;

import com.solardocs.config.AppDataDirectoryConfig;
import com.solardocs.domain.template.DocumentTemplate;
import com.solardocs.infrastructure.persistence.json.JsonFileUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class TemplateManagementService {

    private record Registry(List<DocumentTemplate> templates) {}

    private final AppDataDirectoryConfig dirs;
    private final JsonFileUtils json;

    public TemplateManagementService(AppDataDirectoryConfig dirs, JsonFileUtils json) {
        this.dirs = dirs;
        this.json = json;
    }

    public List<DocumentTemplate> listActive() {
        try {
            Registry r = json.read(dirs.templatesDir().resolve("templates-registry.json"), Registry.class);
            return r == null ? List.of() : r.templates().stream().filter(DocumentTemplate::active).toList();
        } catch (IOException e) { throw new RuntimeException(e); }
    }

    public DocumentTemplate getByCode(String code) {
        return listActive().stream().filter(t -> t.code().equals(code)).findFirst()
                .orElseThrow(() -> new NoSuchElementException("Template not found or inactive: " + code));
    }
}
