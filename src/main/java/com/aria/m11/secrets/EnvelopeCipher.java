package com.aria.m11.secrets;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AES-256-GCM envelope encryption for M11 instance secrets.
 *
 * <p>Key resolution order:
 * <ol>
 *   <li>{@code EARMS_M11_KEK_BASE64} env var — must be base64 of exactly 32 bytes.</li>
 *   <li>{@code earms.m11.kek.base64} system / application property.</li>
 *   <li>Dev fallback — a fixed 32-byte key derived from a constant pepper.
 *       Only enabled when {@code earms.m11.kek.devFallback=true} (default
 *       true so local boot succeeds out-of-the-box). A WARN is logged so
 *       prod misconfigurations are loud.</li>
 * </ol>
 *
 * <p>Ciphertext layout (base64-encoded as one string in DB):
 * {@code [12-byte nonce] || [ciphertext || 16-byte tag]}.
 *
 * <p>{@code kid} is the first 16 chars of SHA-256(KEK-bytes) — a deterministic
 * key identifier that lets the loader pick the right KEK after rotation.
 */
@Component
public class EnvelopeCipher {

    private static final Logger log = LoggerFactory.getLogger(EnvelopeCipher.class);

    /** AES-GCM nonce length (96 bits is the recommended IV size). */
    public static final int NONCE_BYTES = 12;
    /** GCM authentication tag size in bits. */
    public static final int TAG_BITS = 128;
    /** Required raw key size in bytes (AES-256). */
    public static final int KEY_BYTES = 32;

    @Value("${EARMS_M11_KEK_BASE64:}")
    private String envKekBase64;

    @Value("${earms.m11.kek.base64:}")
    private String propKekBase64;

    @Value("${earms.m11.kek.devFallback:true}")
    private boolean devFallbackEnabled;

    /** {@code kid -> raw 32-byte KEK}. The active KEK's {@code kid} is also
     *  returned for new encryptions. Additional KEKs may be loaded for
     *  rotation via {@link #registerKek(String, byte[])}. */
    private final Map<String, byte[]> keksByKid = new ConcurrentHashMap<>();

    private volatile String activeKid;
    private final SecureRandom random = new SecureRandom();

    @PostConstruct
    public void init() {
        byte[] kek = pickKek();
        String kid = kidFor(kek);
        keksByKid.put(kid, kek);
        activeKid = kid;
        log.info("EnvelopeCipher initialised — activeKid={}", kid);
    }

    private byte[] pickKek() {
        if (envKekBase64 != null && !envKekBase64.isBlank()) {
            byte[] decoded = Base64.getDecoder().decode(envKekBase64.trim());
            if (decoded.length != KEY_BYTES) {
                throw new IllegalStateException(
                        "EARMS_M11_KEK_BASE64 must decode to " + KEY_BYTES + " bytes, got " + decoded.length);
            }
            log.info("EnvelopeCipher KEK loaded from env (EARMS_M11_KEK_BASE64)");
            return decoded;
        }
        if (propKekBase64 != null && !propKekBase64.isBlank()) {
            byte[] decoded = Base64.getDecoder().decode(propKekBase64.trim());
            if (decoded.length != KEY_BYTES) {
                throw new IllegalStateException(
                        "earms.m11.kek.base64 must decode to " + KEY_BYTES + " bytes, got " + decoded.length);
            }
            log.info("EnvelopeCipher KEK loaded from property earms.m11.kek.base64");
            return decoded;
        }
        if (!devFallbackEnabled) {
            throw new IllegalStateException(
                    "No M11 KEK configured (set EARMS_M11_KEK_BASE64 or earms.m11.kek.base64)");
        }
        log.warn("EnvelopeCipher KEK absent — DEV FALLBACK in use. "
                + "Set EARMS_M11_KEK_BASE64 to a base64(32-byte) key in production.");
        // Deterministic dev key: SHA-256 of a constant pepper. NOT secret;
        // every dev instance produces the same key, which is the point —
        // ciphertext written in dev decrypts in dev tests.
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest("earms-m11-dev-pepper-do-not-use-in-prod".getBytes());
        } catch (Exception e) {
            throw new IllegalStateException("dev fallback derivation failed", e);
        }
    }

    /** Register an additional KEK (e.g. the previous one during rotation). */
    public void registerKek(String kid, byte[] kek) {
        Objects.requireNonNull(kid, "kid");
        if (kek == null || kek.length != KEY_BYTES) {
            throw new IllegalArgumentException("kek must be " + KEY_BYTES + " bytes");
        }
        keksByKid.put(kid, kek);
    }

    /** Promote a previously-registered KEK to be the active one. */
    public void setActiveKid(String kid) {
        if (!keksByKid.containsKey(kid)) {
            throw new IllegalArgumentException("unknown kid: " + kid);
        }
        activeKid = kid;
    }

    public String getActiveKid() { return activeKid; }

    public Result encrypt(byte[] plaintext) {
        if (plaintext == null) throw new IllegalArgumentException("plaintext required");
        try {
            byte[] kek = keksByKid.get(activeKid);
            byte[] nonce = new byte[NONCE_BYTES];
            random.nextBytes(nonce);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(kek, "AES"),
                    new GCMParameterSpec(TAG_BITS, nonce));
            byte[] ct = cipher.doFinal(plaintext);
            ByteBuffer bb = ByteBuffer.allocate(NONCE_BYTES + ct.length);
            bb.put(nonce);
            bb.put(ct);
            String b64 = Base64.getEncoder().encodeToString(bb.array());
            return new Result(activeKid, b64);
        } catch (Exception e) {
            throw new IllegalStateException("M11 encrypt failed", e);
        }
    }

    public byte[] decrypt(String kid, String ciphertextBase64) {
        if (kid == null || ciphertextBase64 == null) {
            throw new IllegalArgumentException("kid + ciphertext required");
        }
        byte[] kek = keksByKid.get(kid);
        if (kek == null) {
            throw new IllegalStateException("Unknown kid (rotation gap?): " + kid);
        }
        try {
            byte[] all = Base64.getDecoder().decode(ciphertextBase64);
            if (all.length < NONCE_BYTES + 16) {
                throw new IllegalStateException("ciphertext too short");
            }
            byte[] nonce = new byte[NONCE_BYTES];
            byte[] ct = new byte[all.length - NONCE_BYTES];
            System.arraycopy(all, 0, nonce, 0, NONCE_BYTES);
            System.arraycopy(all, NONCE_BYTES, ct, 0, ct.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(kek, "AES"),
                    new GCMParameterSpec(TAG_BITS, nonce));
            return cipher.doFinal(ct);
        } catch (Exception e) {
            throw new IllegalStateException("M11 decrypt failed (kid=" + kid + ")", e);
        }
    }

    static String kidFor(byte[] kek) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] h = md.digest(kek);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(h).substring(0, 16);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /** Encryption result — kid identifies which KEK was used. */
    public record Result(String kid, String ciphertextBase64) {}
}
