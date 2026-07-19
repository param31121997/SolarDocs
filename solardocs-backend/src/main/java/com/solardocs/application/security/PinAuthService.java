package com.solardocs.application.security;

import com.solardocs.config.AppDataDirectoryConfig;
import com.solardocs.domain.security.PinCredential;
import com.solardocs.infrastructure.persistence.json.JsonFileUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class PinAuthService {

    private final AppDataDirectoryConfig dirs;
    private final JsonFileUtils json;

    public PinAuthService(AppDataDirectoryConfig dirs, JsonFileUtils json) {
        this.dirs = dirs;
        this.json = json;
    }

    private java.nio.file.Path file() { return dirs.configDir().resolve("pin.json"); }

    public void setPin(String pin) {
        try {
            byte[] saltBytes = new byte[16];
            new SecureRandom().nextBytes(saltBytes);
            String salt = Base64.getEncoder().encodeToString(saltBytes);
            String hash = hash(pin, salt);
            json.writeAtomic(file(), new PinCredential(hash, salt));
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public boolean verify(String pin) {
        try {
            PinCredential stored = json.read(file(), PinCredential.class);
            if (stored == null) return true;
            return hash(pin, stored.salt()).equals(stored.pinHash());
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public boolean isPinSet() throws IOException {
        return json.read(file(), PinCredential.class) != null;
    }

    private String hash(String pin, String salt) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(Base64.getDecoder().decode(salt));
        byte[] hashed = digest.digest(pin.getBytes());
        return Base64.getEncoder().encodeToString(hashed);
    }
}
