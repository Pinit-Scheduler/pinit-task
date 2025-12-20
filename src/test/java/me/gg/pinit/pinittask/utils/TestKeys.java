package me.gg.pinit.pinittask.utils;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

public final class TestKeys {
    private static final KeyPair KEY_PAIR = generate();

    private TestKeys() {
    }

    private static KeyPair generate() {
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048);
            return gen.generateKeyPair();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static PublicKey publicKey() {
        return KEY_PAIR.getPublic();
    }

    public static PrivateKey privateKey() {
        return KEY_PAIR.getPrivate();
    }
}
