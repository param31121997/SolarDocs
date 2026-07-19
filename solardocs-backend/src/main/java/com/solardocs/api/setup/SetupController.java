package com.solardocs.api.setup;

import com.solardocs.api.common.ApiResponse;
import com.solardocs.config.AppDataDirectoryConfig;
import com.solardocs.config.DataDirectoryBootstrap;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Backs the first-run "choose your data folder" wizard. Note that
 * choosing a new folder here does NOT move the app to that folder
 * immediately - {@link AppDataDirectoryConfig} is resolved once, at
 * application startup. Saving here writes the choice to
 * {@link DataDirectoryBootstrap}, and the app must be restarted for it
 * to take effect (the frontend wizard tells the user this explicitly).
 */
@RestController
@RequestMapping("/api/setup")
public class SetupController {

    private final AppDataDirectoryConfig dirs;

    public SetupController(AppDataDirectoryConfig dirs) {
        this.dirs = dirs;
    }

    public record SetupStatus(boolean configured, String currentDataDir) {}

    @GetMapping("/status")
    public ApiResponse<SetupStatus> status() {
        boolean configured = DataDirectoryBootstrap.isConfigured();
        String currentDataDir = dirs.root().toAbsolutePath().normalize().toString();
        return ApiResponse.ok(new SetupStatus(configured, currentDataDir));
    }

    public record ChooseFolderRequest(String path) {}

    @PostMapping("/data-directory")
    public ApiResponse<String> chooseFolder(@RequestBody ChooseFolderRequest request) {
        String requestedPath = (request.path() == null || request.path().isBlank())
                ? dirs.root().toAbsolutePath().toString()
                : request.path().trim();

        Path target = Path.of(requestedPath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(target);
            // Prove the folder is actually writable before we commit to it -
            // better to fail loudly here than have every future save fail.
            Path probe = target.resolve(".solardocs-write-test");
            Files.writeString(probe, "ok");
            Files.delete(probe);
        } catch (IOException e) {
            throw new RuntimeException("Cannot write to folder: " + target
                    + " - choose a different location or check permissions", e);
        }

        DataDirectoryBootstrap.writeStoredPath(target.toString());
        return ApiResponse.ok(target.toString());
    }
}
