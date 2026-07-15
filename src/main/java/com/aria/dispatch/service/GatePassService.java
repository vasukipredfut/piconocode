package com.aria.dispatch.service;

import com.aria.dispatch.dao.GatePassRepository;
import com.aria.dispatch.model.GatePass;
import com.aria.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

/**
 * M10 gate-pass operations invoked by the workflow engine.
 *
 * <p>Created on first workflow transition (a side-effect of the engine's
 * post-transition hook); updated on every subsequent transition. The
 * gp_number is derived from the workflow instance id so the operation is
 * idempotent on replay.
 */
@Service
public class GatePassService {

    private static final Logger log = LoggerFactory.getLogger(GatePassService.class);

    @Autowired private GatePassRepository repo;

    /**
     * Idempotently upsert the gate-pass record tied to this workflow
     * instance. Creates a row on the first call; updates status on every
     * subsequent call.
     */
    @Transactional("m0PageTransactionManager")
    public GatePass upsertForInstance(Long workflowInstanceId, String workflowCode,
                                       String currentState, Map<String, Object> submittedData) {
        Long tenantId = TenantContext.get();
        Optional<GatePass> existing = repo.findByTenantIdAndWorkflowInstanceId(tenantId, workflowInstanceId);
        GatePass gp = existing.orElseGet(() -> GatePass.builder()
                .tenantId(tenantId)
                .workflowInstanceId(workflowInstanceId)
                .gpNumber("GP-" + tenantId + "-" + workflowInstanceId)
                .flowVariantCode(workflowCode == null ? "M10.TankerInbound" : workflowCode)
                .vehicleNo(extractVehicleNo(submittedData))
                .driverName(extractDriverName(submittedData))
                .driverPhone(extractDriverPhone(submittedData))
                .status("DRAFT")
                .build());

        // Update mutable fields from submitted data on every transition.
        String vehicle = extractVehicleNo(submittedData);
        if (vehicle != null && !vehicle.isBlank()) gp.setVehicleNo(vehicle);
        String dname = extractDriverName(submittedData);
        if (dname != null && !dname.isBlank()) gp.setDriverName(dname);
        String dphone = extractDriverPhone(submittedData);
        if (dphone != null && !dphone.isBlank()) gp.setDriverPhone(dphone);
        if (currentState != null) {
            gp.setStatus(currentState);
            gp.setCurrentStep(currentState);
        }
        // Terminal states freeze the record.
        if (currentState != null && (currentState.equals("EXIT_STAMPED")
                || currentState.equals("REJECTED")
                || currentState.equals("ABORTED")
                || currentState.equals("QC_REJECTED"))) {
            gp.setClosedAt(Instant.now());
        }
        GatePass saved = repo.save(gp);
        log.debug("gate_pass.upsert tenant={} instance={} status={} gpNumber={}",
                tenantId, workflowInstanceId, saved.getStatus(), saved.getGpNumber());
        return saved;
    }

    @Transactional(value = "m0PageTransactionManager", readOnly = true)
    public Optional<GatePass> findByGpNumber(String gpNumber) {
        return repo.findByTenantIdAndGpNumber(TenantContext.get(), gpNumber);
    }

    @Transactional(value = "m0PageTransactionManager", readOnly = true)
    public Optional<GatePass> findByWorkflowInstance(Long workflowInstanceId) {
        return repo.findByTenantIdAndWorkflowInstanceId(TenantContext.get(), workflowInstanceId);
    }

    @Transactional(value = "m0PageTransactionManager", readOnly = true)
    public long count() {
        return repo.countByTenantId(TenantContext.get());
    }

    // ---- field extraction helpers --------------------------------------
    // Forms can post in either snake_case (matching DB columns / generic
    // CRUD payloads) or camelCase (legacy JSON convention). Accept both.
    private String extractVehicleNo(Map<String, Object> bag) {
        return pluck(bag, "vehicleNo", "vehicle_no", "registrationNo", "registration_no", "plate", "vehicleRegNo");
    }

    private String extractDriverName(Map<String, Object> bag) {
        return pluck(bag, "driverName", "driver_name");
    }

    private String extractDriverPhone(Map<String, Object> bag) {
        return pluck(bag, "driverPhone", "driver_phone", "driverMobile", "driver_mobile");
    }

    /** Best-effort field pluck — tries each key at the top level of the bag
     * AND inside any nested page-bag map. Returns the first non-blank match. */
    @SuppressWarnings("unchecked")
    private String pluck(Map<String, Object> bag, String... keys) {
        if (bag == null) return null;
        for (String k : keys) {
            Object v = bag.get(k);
            if (v != null && !v.toString().isBlank()) return v.toString();
        }
        for (Object v : bag.values()) {
            if (v instanceof Map<?, ?> m) {
                for (String k : keys) {
                    Object x = m.get(k);
                    if (x != null && !x.toString().isBlank()) return x.toString();
                }
            }
        }
        return null;
    }
}
