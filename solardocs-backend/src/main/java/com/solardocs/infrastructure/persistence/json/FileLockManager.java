package com.solardocs.infrastructure.persistence.json;

import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * In-process per-file locking. Since SolarDocs V1 is a single-user desktop
 * app talking to a single embedded backend, a JVM-level lock per file path
 * is enough to serialize concurrent read-modify-write sequences.
 */
@Component
public class FileLockManager {

    private final ConcurrentHashMap<Path, ReentrantLock> locks = new ConcurrentHashMap<>();

    public <T> T withLock(Path file, java.util.concurrent.Callable<T> action) {
        ReentrantLock lock = locks.computeIfAbsent(file, f -> new ReentrantLock());
        lock.lock();
        try {
            return action.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }
}
