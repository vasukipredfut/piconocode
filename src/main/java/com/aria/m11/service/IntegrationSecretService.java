package com.aria.m11.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.m11.dao.IntegrationInstanceSecretRepository;
import com.aria.m11.model.IntegrationInstanceSecret;
import com.aria.m11.secrets.EnvelopeCipher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Stores and retrieves per-instance secret bundles via the
 * {@link EnvelopeCipher}. The secret is a JSON object (apiKey, hmacSecret,
 * clientId/clientSecret, etc.) — adapters declare what keys they expect.
 *
 * <p>One row per write — rotation keeps history in
 * {@code integration_instance_secret} (newest first via createdAt).
 */
@Service
public class IntegrationSecretService {

    private static final Logger log = LoggerFactory.getLogger(IntegrationSecretService.class);
    private static final TypeReference<Map<String, Object>> MAP_T = new TypeReference<>() {};

    @Autowired private EnvelopeCipher cipher;
    @Autowired private IntegrationInstanceSecretRepository repo;
    private final ObjectMapper json = new ObjectMapper();

    @Transactional("m11TransactionManager")
    public IntegrationInstanceSecret store(Long instanceId, Map<String, Object> plaintext) {
        if (instanceId == null) throw new IllegalArgumentException("instanceId required");
        if (plaintext == null) plaintext = Map.of();
        try {
            byte[] raw = json.writeValueAsBytes(plaintext);
            EnvelopeCipher.Result enc = cipher.encrypt(raw);
            IntegrationInstanceSecret row = IntegrationInstanceSecret.builder()
                    .instanceId(instanceId)
                    .kid(enc.kid())
                    .ciphertext(enc.ciphertextBase64())
                    .encodedKeyRef("env:EARMS_M11_KEK")
                    .build();
            return repo.save(row);
        } catch (Exception e) {
            throw new IllegalStateException("encrypt-and-store failed for instance " + instanceId, e);
        }
    }

    @Transactional(value = "m11TransactionManager", readOnly = true)
    public Optional<Map<String, Object>> loadDecrypted(Long instanceId) {
        if (instanceId == null) return Optional.empty();
        Optional<IntegrationInstanceSecret> rowOpt = repo.findFirstByInstanceIdOrderByCreatedAtDesc(instanceId);
        if (rowOpt.isEmpty()) return Optional.empty();
        IntegrationInstanceSecret row = rowOpt.get();
        try {
            byte[] raw = cipher.decrypt(row.getKid(), row.getCiphertext());
            Map<String, Object> map = json.readValue(new String(raw, StandardCharsets.UTF_8), MAP_T);
            return Optional.of(map);
        } catch (Exception e) {
            log.warn("M11 secret decrypt failed for instance {}: {}", instanceId, e.getMessage());
            return Optional.empty();
        }
    }

    /** Convenience for callers that don't care about the difference between
     *  "no secret stored" and "decrypt failed". */
    public Map<String, Object> loadOrEmpty(Long instanceId) {
        return loadDecrypted(instanceId).orElse(new HashMap<>());
    }
}
