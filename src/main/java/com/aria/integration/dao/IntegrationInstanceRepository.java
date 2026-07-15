package com.aria.integration.dao;

import com.aria.integration.model.IntegrationInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IntegrationInstanceRepository extends JpaRepository<IntegrationInstance, Long> {

    List<IntegrationInstance> findByTenantIdOrderByCreatedAtDesc(Long tenantId);

    List<IntegrationInstance> findByTenantIdAndStatusOrderByCreatedAtDesc(Long tenantId, String status);

    Optional<IntegrationInstance> findByTenantIdAndCode(Long tenantId, String code);

    Optional<IntegrationInstance> findByIdAndTenantId(Long id, Long tenantId);

    boolean existsByTenantIdAndCode(Long tenantId, String code);
}
