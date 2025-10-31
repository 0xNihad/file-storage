package com.filestore.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
public class TokenGenerator {

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder().withoutPadding();

    public String generateToken(int length) {
        byte[] bytes = new byte[length];
        secureRandom.nextBytes(bytes);
        return base64Encoder.encodeToString(bytes);
    }

    public String generateShareToken() {
        return generateToken(9);
    }

    public String generateDeleteToken() {
        return "del_" + generateToken(12);
    }

    public String generateStorageFileName(String originalExtension) {
        return generateToken(16) + "." + originalExtension;
    }
}
