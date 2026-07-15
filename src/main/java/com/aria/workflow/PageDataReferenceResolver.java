package com.aria.workflow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.workflow.dao.WorkflowInstanceRepository;
import com.aria.workflow.model.WorkflowInstance;
import com.aria.security.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

/**
 * Resolves {@code pageData.<pageCode>.<fieldCode>} expressions inside workflow
 * guards / actions against a workflow instance's accumulated submitted-data
 * payload.
 *
 * <p>Wired path: the engine + REST controllers populate
 * {@link WorkflowInstance#getSubmittedDataJson()} on every page submission;
 * this resolver loads the instance row, parses that JSON, and walks the
 * (pageCode, fieldCode) path. Missing path throws
 * {@link PageDataMissingException} so callers can disambiguate "not submitted
 * yet" from "instance gone".
 */
@Service
public class PageDataReferenceResolver {

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired private WorkflowInstanceRepository instances;

    /**
     * In-memory overload (kept for unit tests and engine-internal callers
     * that already have the bag inflated).
     */
    public Optional<Object> resolve(String pageCode, String fieldCode, Map<String, Map<String, Object>> submittedData) {
        if (submittedData == null) {
            throw new PageDataMissingException(
                    "pageData." + pageCode + "." + fieldCode + " — workflow instance has no submitted data");
        }
        Map<String, Object> pageBag = submittedData.get(pageCode);
        if (pageBag == null || !pageBag.containsKey(fieldCode)) {
            throw new PageDataMissingException(
                    "pageData." + pageCode + "." + fieldCode + " — not yet submitted");
        }
        return Optional.ofNullable(pageBag.get(fieldCode));
    }

    /** Resolve against a workflow_instance row by id (tenant-scoped). */
    @Transactional(value = "m0PageTransactionManager", readOnly = true)
    public Optional<Object> resolveForInstance(String pageCode, String fieldCode, Long instanceId) {
        Long tenantId = TenantContext.get();
        WorkflowInstance inst = instances.findByIdAndTenantId(instanceId, tenantId)
                .orElseThrow(() -> new PageDataMissingException(
                        "pageData." + pageCode + "." + fieldCode + " — instance " + instanceId + " not found"));
        return resolveFromJson(pageCode, fieldCode, inst.getSubmittedDataJson());
    }

    /** Raw JSON overload — matches the workflow_instance.submitted_data_json column shape. */
    public Optional<Object> resolveFromJson(String pageCode, String fieldCode, String submittedDataJson) {
        if (submittedDataJson == null || submittedDataJson.isBlank()) {
            throw new PageDataMissingException(
                    "pageData." + pageCode + "." + fieldCode + " — workflow instance has no submitted data");
        }
        try {
            JsonNode root = mapper.readTree(submittedDataJson);
            JsonNode pageNode = root.get(pageCode);
            if (pageNode == null || !pageNode.isObject()) {
                throw new PageDataMissingException(
                        "pageData." + pageCode + "." + fieldCode + " — no submission for page");
            }
            JsonNode val = pageNode.get(fieldCode);
            if (val == null || val.isMissingNode() || val.isNull()) {
                throw new PageDataMissingException(
                        "pageData." + pageCode + "." + fieldCode + " — not yet submitted");
            }
            if (val.isTextual()) return Optional.of(val.asText());
            if (val.isInt()) return Optional.of(val.asInt());
            if (val.isLong()) return Optional.of(val.asLong());
            if (val.isDouble() || val.isFloatingPointNumber()) return Optional.of(val.asDouble());
            if (val.isBoolean()) return Optional.of(val.asBoolean());
            return Optional.of(val.toString());
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new PageDataMissingException(
                    "pageData." + pageCode + "." + fieldCode + " — malformed submitted data JSON");
        }
    }
}
