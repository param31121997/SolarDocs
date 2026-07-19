package com.solardocs.infrastructure.licensing;

import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Enumeration;

@Component
public class MachineFingerprintProvider {

    public String fingerprint() {
        try {
            StringBuilder raw = new StringBuilder();
            raw.append(System.getProperty("os.name"));
            raw.append(System.getProperty("user.name"));

            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                byte[] mac = ni.getHardwareAddress();
                if (mac != null && mac.length > 0) {
                    for (byte b : mac) raw.append(String.format("%02X", b));
                    break;
                }
            }
            if (raw.length() < 20) raw.append(InetAddress.getLocalHost().getHostName());

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.substring(0, 32);
        } catch (Exception e) {
            throw new RuntimeException("Could not compute machine fingerprint", e);
        }
    }
}
