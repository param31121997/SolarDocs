package com.solardocs.api.backup;

import com.solardocs.api.common.ApiResponse;
import com.solardocs.infrastructure.backup.ZipBackupService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/backup")
public class BackupController {

    private final ZipBackupService backupService;

    public BackupController(ZipBackupService backupService) {
        this.backupService = backupService;
    }

    @PostMapping("/create")
    public ApiResponse<String> create() {
        return ApiResponse.ok(backupService.createBackup().getFileName().toString());
    }

    @GetMapping("/list")
    public ApiResponse<List<String>> list() {
        return ApiResponse.ok(backupService.listBackups().stream().map(p -> p.getFileName().toString()).toList());
    }

    public record RestoreRequest(String fileName) {}

    @PostMapping("/restore")
    public ApiResponse<Void> restore(@RequestBody RestoreRequest req) {
        backupService.createBackup();
        backupService.restore(req.fileName());
        return ApiResponse.ok(null);
    }
}
