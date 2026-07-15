package com.aria.integration.secrets;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Field-level secret codec — wraps {@link EnvelopeCipher} for the
 * substrate CRUD path.
 *
 * <p>Contract:
 * <ul>
 *   <li>{@link #encryptIfPlaintext(String)} — encodes a plaintext value as
 *       {@code enc$<kid>$<base64-ciphertext>} on write; idempotent (already-
 *       encrypted values pass through).</li>
 *   <li>{@link #decryptIfEncrypted(String)} — recovers plaintext on read;
 *       leaves untagged values alone (backwards compat with pre-encryption
 *       rows and the {@code no-key-needed-for-local-ollama} dev seed).</li>
 *   <li>{@link #isSecretField(String, String)} — resolves whether an
 *       entity's field should be treated as a secret. Two signals:
 *       <ol>
 *         <li>{@code entity_field_registry.options_json} contains
 *             {@code "secret": true} (the substrate convention V84 set).</li>
 *         <li>The column name is in {@link #ALWAYS_SECRET_COLUMNS} — a
 *             safety net for dev DBs where V84's registry rows drifted or
 *             never ran (so we still encrypt {@code api_key_ciphertext}
 *             regardless of registry state).</li>
 *       </ol>
 *   </li>
 *   <li>{@link #mask(String)} — returns a fixed sentinel for API responses
 *       so ciphertext never leaves the server.</li>
 * </ul>
 *
 * <p>Callers: {@code GenericEntityController} (write + read), {@code LlmProxy}
 * (decrypt api_key on the way to a provider), {@code AiCredentialTestController}
 * (same).
 */
@Component
public class TenantSecretCodec {

    private static final Logger log = LoggerFactory.getLogger(TenantSecretCodec.class);
    private static final ObjectMapper JSON = new ObjectMapper();

    /** Wire prefix that marks an encrypted value. Format: {@code enc$<kid>$<base64ct>}. */
    public static final String PREFIX = "enc$";

    /** Value returned to the browser in place of any secret column. */
    public static final String MASK = "***";

    /**
     * Belt-and-suspenders allowlist — these columns are ALWAYS treated as
     * secret even if the entity_field_registry row doesn't say so. Prevents
     * silent leaks on dev DBs where the registry drifted.
     */
    private static final Set<String> ALWAYS_SECRET_COLUMNS = Set.of(
            "api_key_ciphertext",
            "password_ciphertext",
            "secret_ciphertext",
            "client_secret_ciphertext"
    );

    private final EnvelopeCipher cipher;
    private final JdbcTemplate jdbc;
    private final Map<String, Set<String>> secretFieldsByEntity = new ConcurrentHashMap<>();

    public TenantSecretCodec(EnvelopeCipher cipher, JdbcTemplate jdbc) {
        this.cipher = cipher;
        this.jdbc = jdbc;
    }

    // ------------------------------------------------------------------
    //  Encode / decode
    // ------------------------------------------------------------------

    /** Returns the encrypted wire form, or the input unchanged if it's null / blank / already tagged. */
    public String encryptIfPlaintext(String plaintext) {
        if (plaintext == null || plaintext.isBlank()) return plaintext;
        if (plaintext.startsWith(PREFIX)) return plaintext;
        try {
            EnvelopeCipher.Result r = cipher.encrypt(plaintext.getBytes(StandardCharsets.UTF_8));
            return PREFIX + r.kid() + "$" + r.ciphertextBase64();
        } catch (Exception e) {
            log.warn("TenantSecretCodec.encrypt failed — storing plaintext (fix KEK config): {}", e.getMessage());
            return plaintext;
        }
    }

    /** Returns the plaintext, or the input unchanged if it isn't tagged. */
    public String decryptIfEncrypted(String value) {
        if (value == null || !value.startsWith(PREFIX)) return value;
        String tail = value.substring(PREFIX.length());
        int sep = tail.indexOf('$');
        if (sep <= 0 || sep >= tail.length() - 1) return value;
        String kid = tail.substring(0, sep);
        String ct  = tail.substring(sep + 1);
        try {
            return new String(cipher.decrypt(kid, ct), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("TenantSecretCodec.decrypt failed (kid={}): {}", kid, e.getMessage());
            return value;
        }
    }

    // ------------------------------------------------------------------
    //  Field classification
    // ------------------------------------------------------------------

    /** True if the field should be encrypted on write and masked on read. */
    public boolean isSecretField(String entityCode, String fieldCode) {
        if (fieldCode == null) return false;
        if (ALWAYS_SECRET_COLUMNS.contains(fieldCode)) return true;
        if (entityCode == null) return false;
        return getSecretFields(entityCode).contains(fieldCode);
    }

    /** Fixed sentinel for API responses. */
    public String mask(Object anything) {
        return MASK;
    }

    private Set<String> getSecretFields(String entityCode) {
        return secretFieldsByEntity.computeIfAbsent(entityCode, code -> {
            try {
                List<Map<String, Object>> rows = jdbc.queryForList(
                        "SELECT field_code, options_json FROM entity_field_registry " +
                        "WHERE entity_code=?", code);
                Set<String> out = new HashSet<>();
                for (Map<String, Object> r : rows) {
                    Object oj = r.get("options_json");
                    if (oj == null) continue;
                    try {
                        JsonNode n = JSON.readTree(oj.toString());
                        if (n.path("secret").asBoolean(false)) {
                            out.add((String) r.get("field_code"));
                        }
                    } catch (Exception ignore) { /* malformed json — skip */ }
                }
                return Collections.unmodifiableSet(out);
            } catch (Exception e) {
                // Registry table may not exist on very old dev DBs — allowlist
                // still catches api_key_ciphertext etc.
                return Collections.emptySet();
            }
        });
    }

    /** Test hook — drop the registry cache after a migration that adds new secret fields. */
    public void invalidateCache() { secretFieldsByEntity.clear(); }
}
