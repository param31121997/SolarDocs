package com.solardocs.api.setup;

import com.solardocs.api.common.ApiResponse;
import com.solardocs.config.AppDataDirectoryConfig;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/setup")
public class SetupController {

    private final AppDataDirectoryConfig dirs;

    public SetupController(AppDataDirectoryConfig dirs) {
        this.dirs = dirs;
    }

    @GetMapping("/status")
    public ApiResponse<Boolean> isConfigured() {
        return ApiResponse.ok(Files.exists(dirs.configDir().resolve("config.json")));
    }

    public record ChooseFolderRequest(String path) {}

    @PostMapping("/data-directory")
    public ApiResponse<String> chooseFolder(@RequestBody ChooseFolderRequest req) {
        Path chosen = Path.of(req.path());
        return ApiResponse.ok(chosen.toString());
    }
}
