package com.solardocs.infrastructure.backup;

import com.solardocs.config.AppDataDirectoryConfig;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Component
public class ZipBackupService {

    private final AppDataDirectoryConfig dirs;

    public ZipBackupService(AppDataDirectoryConfig dirs) {
        this.dirs = dirs;
    }

    public Path createBackup() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmm"));
        Path zipFile = dirs.backupDir().resolve("SolarDocs-Backup-" + timestamp + ".zip");
        try {
            Files.createDirectories(dirs.backupDir());
            try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFile))) {
                for (Path source : new Path[]{ dirs.configDir(), dirs.indexesDir(), dirs.customersDir(), dirs.templatesDir(), dirs.reportsDir() }) {
                    if (Files.notExists(source)) continue;
                    try (Stream<Path> walk = Files.walk(source)) {
                        for (Path file : walk.filter(Files::isRegularFile).toList()) {
                            String entryName = dirs.root().relativize(file).toString().replace('\\', '/');
                            zos.putNextEntry(new ZipEntry(entryName));
                            Files.copy(file, zos);
                            zos.closeEntry();
                        }
                    }
                }
            }
            return zipFile;
        } catch (IOException e) {
            throw new RuntimeException("Backup failed", e);
        }
    }

    public java.util.List<Path> listBackups() {
        try {
            if (Files.notExists(dirs.backupDir())) return java.util.List.of();
            try (Stream<Path> files = Files.list(dirs.backupDir())) {
                return files.filter(p -> p.toString().endsWith(".zip"))
                        .sorted(java.util.Comparator.comparing(Path::toString).reversed())
                        .toList();
            }
        } catch (IOException e) { throw new RuntimeException(e); }
    }

    public void restore(String backupFileName) {
        Path zipFile = dirs.backupDir().resolve(backupFileName);
        if (Files.notExists(zipFile)) throw new java.util.NoSuchElementException("Backup not found: " + backupFileName);

        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path target = dirs.root().resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(target);
                } else {
                    Files.createDirectories(target.getParent());
                    Files.copy((InputStream) zis, target, StandardCopyOption.REPLACE_EXISTING);
                }
                zis.closeEntry();
            }
        } catch (IOException e) {
            throw new RuntimeException("Restore failed for " + backupFileName, e);
        }
    }
}
