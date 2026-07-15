package com.aria.m10.dao;

import com.aria.m10.model.GatePass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GatePassRepository extends JpaRepository<GatePass, Long> {

    Optional<GatePass> findByTenantIdAndGpNumber(Long tenantId, String gpNumber);

    Optional<GatePass> findByTenantIdAndWorkflowInstanceId(Long tenantId, Long workflowInstanceId);

    long countByTenantId(Long tenantId);

    long countByTenantIdAndStatus(Long tenantId, String status);
}
