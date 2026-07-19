package com.solardocs.infrastructure.licensing;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class LicenseKeyValidator {

    public record LicensePayload(String vendorId, String machineFingerprint, String expiresAt) {}

    /**
     * A license key is: base64(payload) + "." + base64(signature-of-payload).
     * Verifies the signature against the bundled public key, then checks the
     * fingerprint and expiry match this machine.
     */
    public boolean isValid(String licenseKey, String currentFingerprint) {
        try {
            String[] parts = licenseKey.split("\\.");
            if (parts.length != 2) return false;

            byte[] payloadBytes = Base64.getDecoder().decode(parts[0]);
            byte[] signatureBytes = Base64.getDecoder().decode(parts[1]);
            String payload = new String(payloadBytes);

            PublicKey publicKey = loadPublicKey();
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(publicKey);
            sig.update(payloadBytes);
            if (!sig.verify(signatureBytes)) return false;

            String[] fields = payload.split("\\|");
            if (fields.length != 3) return false;
            String machineFingerprint = fields[1];
            String expiresAt = fields[2];

            if (!machineFingerprint.equals(currentFingerprint)) return false;
            return java.time.LocalDate.now().isBefore(java.time.LocalDate.parse(expiresAt).plusDays(1));
        } catch (Exception e) {
            return false;
        }
    }

    private PublicKey loadPublicKey() throws Exception {
        try (var in = new ClassPathResource("license/public.pem").getInputStream()) {
            String pem = new String(in.readAllBytes())
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] decoded = Base64.getDecoder().decode(pem);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(new X509EncodedKeySpec(decoded));
        }
    }
}
