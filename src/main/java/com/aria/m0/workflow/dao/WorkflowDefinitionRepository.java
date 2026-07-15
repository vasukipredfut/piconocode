package com.aria.m0.workflow.dao;

import com.aria.m0.workflow.model.WorkflowDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkflowDefinitionRepository extends JpaRepository<WorkflowDefinition, Long> {

    @Query("SELECT w FROM WorkflowDefinition w " +
            "WHERE w.tenantId = :tenantId AND w.code = :code AND w.status = 'PUBLISHED' " +
            "ORDER BY w.version DESC")
    java.util.List<WorkflowDefinition> findLatestPublished(
            @Param("tenantId") Long tenantId, @Param("code") String code);

    default Optional<WorkflowDefinition> findLatestPublishedSingle(Long tenantId, String code) {
        return findLatestPublished(tenantId, code).stream().findFirst();
    }

    Optional<WorkflowDefinition> findByTenantIdAndCodeAndVersion(Long tenantId, String code, Integer version);
}
