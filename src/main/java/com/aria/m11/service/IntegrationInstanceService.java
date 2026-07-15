package com.aria.m11.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.m11.dao.IntegrationInstanceRepository;
import com.aria.m11.dao.IntegrationInstanceVersionRepository;
import com.aria.m11.dto.IntegrationInstanceRequest;
import com.aria.m11.model.IntegrationConnectorCatalog;
import com.aria.m11.model.IntegrationInstance;
import com.aria.m11.model.IntegrationInstanceVersion;
import com.aria.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * CRUD + lifecycle ops for {@link IntegrationInstance}. Validates incoming
 * {@code configJson} against the catalog row's {@code configSchemaJson}
 * with a lightweight structural check (required-fields + presence). Round
 * 1H.2 swaps the validator for full Ajv via the m0 expression sandbox.
 */
@Service
public class IntegrationInstanceService {

    private static final Logger log = LoggerFactory.getLogger(IntegrationInstanceService.class);
    private static final TypeReference<Map<String, Object>> MAP_T = new TypeReference<>() {};

    @Autowired private IntegrationInstanceRepository instances;
    @Autowired private IntegrationInstanceVersionRepository versions;
    @Autowired private IntegrationCatalogService catalog;
    @Autowired private IntegrationSecretService secrets;
    private final ObjectMapper json = new ObjectMapper();

    // ---------------------------------------------------------- create
    @Transactional("m11TransactionManager")
    public IntegrationInstance create(IntegrationInstanceRequest req) {
        Long tenantId = TenantContext.get();
        validateCode(req.getCode());
        if (instances.existsByTenantIdAndCode(tenantId, req.getCode())) {
            throw new InstanceConflictException("M11_INSTANCE_CODE_DUPLICATE: " + req.getCode());
        }
        IntegrationConnectorCatalog cat = requireCatalog(req.getConnectorCode());
        validateConfig(cat, req.getConfigJson());

        IntegrationInstance inst = IntegrationInstance.builder()
                .tenantId(tenantId)
                .code(req.getCode())
                .connectorCode(cat.getCode())
                .connectorVersion(cat.getVersion())
                .name(req.getName() == null ? req.getCode() : req.getName())
                .description(req.getDescription())
                .status(IntegrationInstance.STATUS_DRAFT)
                .currentVersion(1)
                .configJson(writeJson(req.getConfigJson()))
                .mappingJson(req.getMappingJson() == null ? null : writeJson(req.getMappingJson()))
                .mappedWorkflowCodesJson(writeJson(req.getMappedWorkflowCodes() == null
                        ? List.of() : req.getMappedWorkflowCodes()))
                .dataResidencyFlag(req.getDataResidencyFlag() == null
                        ? "ANY" : req.getDataResidencyFlag())
                .rateLimitRps(req.getRateLimitRps())
                .errorCount24h(0)
                .build();
        inst = instances.save(inst);

        IntegrationInstanceVersion v = IntegrationInstanceVersion.builder()
                .instanceId(inst.getId()).version(1).configJson(inst.getConfigJson())
                .mappingJson(inst.getMappingJson()).status("DRAFT").build();
        versions.save(v);

        if (req.getCredentials() != null && !req.getCredentials().isEmpty()) {
            secrets.store(inst.getId(), req.getCredentials());
        }
        log.info("M11.create tenant={} instance={} code={} connector={}",
                tenantId, inst.getId(), inst.getCode(), inst.getConnectorCode());
        return inst;
    }

    // ---------------------------------------------------------- read
    public List<IntegrationInstance> listByTenant() {
        return instances.findByTenantIdOrderByCreatedAtDesc(TenantContext.get());
    }

    public List<IntegrationInstance> listByTenantAndStatus(String status) {
        return instances.findByTenantIdAndStatusOrderByCreatedAtDesc(TenantContext.get(), status);
    }

    public Optional<IntegrationInstance> find(Long id) {
        return instances.findByIdAndTenantId(id, TenantContext.get());
    }

    public IntegrationInstance require(Long id) {
        return find(id).orElseThrow(() -> new InstanceNotFoundException(
                "instance " + id + " not found for tenant " + TenantContext.get()));
    }

    // ---------------------------------------------------------- update
    @Transactional("m11TransactionManager")
    public IntegrationInstance updateDraft(Long id, IntegrationInstanceRequest req) {
        IntegrationInstance inst = require(id);
        IntegrationConnectorCatalog cat = requireCatalog(inst.getConnectorCode());
        if (req.getConfigJson() != null) {
            validateConfig(cat, req.getConfigJson());
            inst.setConfigJson(writeJson(req.getConfigJson()));
        }
        if (req.getName() != null) inst.setName(req.getName());
        if (req.getDescription() != null) inst.setDescription(req.getDescription());
        if (req.getMappingJson() != null) inst.setMappingJson(writeJson(req.getMappingJson()));
        if (req.getMappedWorkflowCodes() != null) {
            inst.setMappedWorkflowCodesJson(writeJson(req.getMappedWorkflowCodes()));
        }
        if (req.getDataResidencyFlag() != null) inst.setDataResidencyFlag(req.getDataResidencyFlag());
        if (req.getRateLimitRps() != null) inst.setRateLimitRps(req.getRateLimitRps());
        // Bump the in-flight draft version (no row split; current_version
        // is mutated on publish).
        IntegrationInstance saved = instances.save(inst);
        if (req.getCredentials() != null && !req.getCredentials().isEmpty()) {
            secrets.store(saved.getId(), req.getCredentials());
        }
        return saved;
    }

    // ---------------------------------------------------------- publish
    @Transactional("m11TransactionManager")
    public IntegrationInstanceVersion publish(Long id, String rationale, Long actorUserId) {
        IntegrationInstance inst = require(id);
        int next = inst.getCurrentVersion() + 1;
        IntegrationInstanceVersion v = IntegrationInstanceVersion.builder()
                .instanceId(inst.getId()).version(next)
                .configJson(inst.getConfigJson()).mappingJson(inst.getMappingJson())
                .rationale(rationale).status("PUBLISHED")
                .publishedAt(Instant.now()).publishedBy(actorUserId)
                .build();
        v = versions.save(v);
        inst.setCurrentVersion(next);
        if (IntegrationInstance.STATUS_DRAFT.equals(inst.getStatus())) {
            inst.setStatus(IntegrationInstance.STATUS_ENROLLED);
        }
        instances.save(inst);
        return v;
    }

    @Transactional("m11TransactionManager")
    public IntegrationInstanceVersion rollback(Long id, int toVersion, Long actorUserId) {
        IntegrationInstance inst = require(id);
        IntegrationInstanceVersion src = versions.findByInstanceIdAndVersion(id, toVersion)
                .orElseThrow(() -> new InstanceNotFoundException(
                        "version " + toVersion + " not found for instance " + id));
        int next = inst.getCurrentVersion() + 1;
        IntegrationInstanceVersion v = IntegrationInstanceVersion.builder()
                .instanceId(inst.getId()).version(next)
                .configJson(src.getConfigJson()).mappingJson(src.getMappingJson())
                .rationale("rolled back from v" + toVersion).status("PUBLISHED")
                .publishedAt(Instant.now()).publishedBy(actorUserId)
                .build();
        v = versions.save(v);
        inst.setCurrentVersion(next);
        inst.setConfigJson(src.getConfigJson());
        inst.setMappingJson(src.getMappingJson());
        instances.save(inst);
        return v;
    }

    // ---------------------------------------------------------- lifecycle
    @Transactional("m11TransactionManager")
    public IntegrationInstance enable(Long id) {
        IntegrationInstance inst = require(id);
        if (IntegrationInstance.STATUS_RETIRED.equals(inst.getStatus())) {
            throw new InvalidLifecycleException("cannot enable a retired instance");
        }
        inst.setStatus(IntegrationInstance.STATUS_ACTIVE);
        inst.setStatusReason(null);
        return instances.save(inst);
    }

    @Transactional("m11TransactionManager")
    public IntegrationInstance disable(Long id, String reason) {
        IntegrationInstance inst = require(id);
        inst.setStatus(IntegrationInstance.STATUS_DISABLED);
        inst.setStatusReason(reason);
        return instances.save(inst);
    }

    @Transactional("m11TransactionManager")
    public IntegrationInstance retire(Long id) {
        IntegrationInstance inst = require(id);
        inst.setStatus(IntegrationInstance.STATUS_RETIRED);
        return instances.save(inst);
    }

    @Transactional("m11TransactionManager")
    public void markHealth(Long id, boolean ok, String reason) {
        IntegrationInstance inst = require(id);
        inst.setLastHealthAt(Instant.now());
        if (ok) {
            if (IntegrationInstance.STATUS_DEGRADED.equals(inst.getStatus())
                    || IntegrationInstance.STATUS_DOWN.equals(inst.getStatus())) {
                inst.setStatus(IntegrationInstance.STATUS_ACTIVE);
            }
            inst.setStatusReason(null);
        } else {
            if (IntegrationInstance.STATUS_ACTIVE.equals(inst.getStatus())) {
                inst.setStatus(IntegrationInstance.STATUS_DEGRADED);
            }
            inst.setStatusReason(reason);
            inst.setErrorCount24h(inst.getErrorCount24h() + 1);
        }
        instances.save(inst);
    }

    // ---------------------------------------------------------- helpers
    private IntegrationConnectorCatalog requireCatalog(String code) {
        return catalog.findLatestByCode(code).orElseThrow(() ->
                new ConnectorUnknownException("Unknown connector code: " + code));
    }

    private void validateCode(String code) {
        if (code == null || !code.matches("^[A-Za-z][A-Za-z0-9_.-]{2,47}$")) {
            throw new InvalidConfigException(
                    "M11_INSTANCE_CODE_INVALID: must match ^[A-Za-z][A-Za-z0-9_.-]{2,47}$");
        }
    }

    /** Lightweight structural validation: walks the catalog schema's
     *  {@code required} list and confirms each path is present in the
     *  candidate config. Sufficient for Round 1H.1; Ajv-grade enforcement
     *  comes with the validator overhaul in 1H.2. */
    @SuppressWarnings("unchecked")
    void validateConfig(IntegrationConnectorCatalog cat, Map<String, Object> config) {
        if (config == null) throw new InvalidConfigException("M11_CONFIG_REQUIRED");
        Map<String, Object> schema;
        try { schema = json.readValue(cat.getConfigSchemaJson(), MAP_T); }
        catch (Exception e) {
            log.warn("Catalog {} has malformed config_schema_json: {}", cat.getCode(), e.getMessage());
            return;
        }
        Object required = schema.get("required");
        if (required instanceof List<?> list) {
            List<String> missing = new ArrayList<>();
            for (Object o : list) {
                if (o == null) continue;
                String k = String.valueOf(o);
                if (!config.containsKey(k) || config.get(k) == null
                        || (config.get(k) instanceof String s && s.isBlank())) {
                    missing.add(k);
                }
            }
            if (!missing.isEmpty()) {
                throw new InvalidConfigException(
                        "M11_CONFIG_SCHEMA_VIOLATION: missing required fields " + missing);
            }
        }
    }

    private String writeJson(Object o) {
        try { return json.writeValueAsString(o); }
        catch (Exception e) { throw new IllegalArgumentException("config not serialisable", e); }
    }

    // ---------- exception types (mapped to 4xx by the controller) ----------
    public static class InstanceNotFoundException extends RuntimeException {
        public InstanceNotFoundException(String m) { super(m); }
    }
    public static class InstanceConflictException extends RuntimeException {
        public InstanceConflictException(String m) { super(m); }
    }
    public static class ConnectorUnknownException extends RuntimeException {
        public ConnectorUnknownException(String m) { super(m); }
    }
    public static class InvalidConfigException extends RuntimeException {
        public InvalidConfigException(String m) { super(m); }
    }
    public static class InvalidLifecycleException extends RuntimeException {
        public InvalidLifecycleException(String m) { super(m); }
    }
}
