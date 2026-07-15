package com.aria.m11.secrets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link EnvelopeCipher} — AES-256-GCM round trip, kid
 * identification, rotation (new active KEK can decrypt old + new), error
 * paths for unknown kid / short ciphertext.
 */
class EnvelopeCipherTest {

    EnvelopeCipher cipher;

    @BeforeEach
    void setUp() throws Exception {
        cipher = new EnvelopeCipher();
        inject(cipher, "envKekBase64", "");
        inject(cipher, "propKekBase64", "");
        inject(cipher, "devFallbackEnabled", true);
        cipher.init();
    }

    @Test
    @DisplayName("round trip — encrypt then decrypt returns the original plaintext")
    void roundTrip() {
        byte[] pt = "hello-m11".getBytes(StandardCharsets.UTF_8);
        EnvelopeCipher.Result enc = cipher.encrypt(pt);
        assertThat(enc.kid()).isNotBlank();
        assertThat(enc.ciphertextBase64()).isNotBlank();
        byte[] back = cipher.decrypt(enc.kid(), enc.ciphertextBase64());
        assertThat(back).isEqualTo(pt);
    }

    @Test
    @DisplayName("kid is deterministic for a given KEK")
    void kidDeterminism() throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] dev = md.digest("earms-m11-dev-pepper-do-not-use-in-prod".getBytes());
        String kid = EnvelopeCipher.kidFor(dev);
        assertThat(cipher.getActiveKid()).isEqualTo(kid);
    }

    @Test
    @DisplayName("rotation — old ciphertext still decryptable after a new active KEK")
    void rotationKeepsOldKid() throws Exception {
        // Encrypt under the dev KEK first.
        EnvelopeCipher.Result first = cipher.encrypt("secret-v1".getBytes());

        // Register a new KEK and promote it.
        byte[] newKek = "0123456789ABCDEF0123456789ABCDEF".getBytes();
        String newKid = "kek-rot-v2";
        cipher.registerKek(newKid, newKek);
        cipher.setActiveKid(newKid);

        // New encryption uses the new KEK; old ciphertext still decrypts.
        EnvelopeCipher.Result second = cipher.encrypt("secret-v2".getBytes());
        assertThat(second.kid()).isEqualTo(newKid);

        assertThat(new String(cipher.decrypt(first.kid(), first.ciphertextBase64())))
                .isEqualTo("secret-v1");
        assertThat(new String(cipher.decrypt(second.kid(), second.ciphertextBase64())))
                .isEqualTo("secret-v2");
    }

    @Test
    @DisplayName("unknown kid is a hard error")
    void unknownKidIsFailFast() {
        EnvelopeCipher.Result enc = cipher.encrypt("x".getBytes());
        assertThatThrownBy(() -> cipher.decrypt("nope", enc.ciphertextBase64()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Unknown kid");
    }

    @Test
    @DisplayName("dev fallback disabled + no env => init refuses to start")
    void noKekConfigured() throws Exception {
        EnvelopeCipher fresh = new EnvelopeCipher();
        inject(fresh, "envKekBase64", "");
        inject(fresh, "propKekBase64", "");
        inject(fresh, "devFallbackEnabled", false);
        assertThatThrownBy(fresh::init).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("env KEK with wrong byte length is rejected")
    void envKekLengthValidated() throws Exception {
        EnvelopeCipher fresh = new EnvelopeCipher();
        inject(fresh, "envKekBase64", Base64.getEncoder().encodeToString(new byte[16]));
        inject(fresh, "propKekBase64", "");
        inject(fresh, "devFallbackEnabled", true);
        assertThatThrownBy(fresh::init)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32 bytes");
    }

    // ---- helpers ----
    private static void inject(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }
}
