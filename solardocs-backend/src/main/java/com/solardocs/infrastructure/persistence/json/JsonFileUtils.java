package com.solardocs.infrastructure.persistence.json;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;

@Component
public class JsonFileUtils {

    // FAIL_ON_UNKNOWN_PROPERTIES off on purpose: customer.json's shape has
    // changed over time (e.g. email/aadhaarNumber moving from nested
    // plantDetails to top-level fields). Without this, older files with
    // now-unrecognized properties would fail to load entirely instead of
    // just ignoring the stale field.
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.INDENT_OUTPUT)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public ObjectMapper mapper() { return mapper; }

    /** Read and deserialize a JSON file. Returns null if the file does not exist. */
    public <T> T read(Path file, Class<T> type) throws IOException {
        if (Files.notExists(file)) return null;
        return mapper.readValue(file.toFile(), type);
    }

    /**
     * Atomically write an object as JSON: write to a temp file in the same
     * directory, then move it into place with ATOMIC_MOVE. A crash mid-write
     * leaves the original file untouched — never a half-written JSON file.
     */
    public void writeAtomic(Path targetFile, Object value) throws IOException {
        Files.createDirectories(targetFile.getParent());
        Path tmp = Files.createTempFile(targetFile.getParent(), targetFile.getFileName().toString(), ".tmp");
        try {
            mapper.writeValue(tmp.toFile(), value);
            Files.move(tmp, targetFile,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } finally {
            Files.deleteIfExists(tmp);
        }
    }
}
