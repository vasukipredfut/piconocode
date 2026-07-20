package com.aria.workflow.dao;

import com.aria.workflow.model.WorkflowDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkflowDefinitionRepository extends JpaRepository<WorkflowDefinition, Long> {

    /**
     * Latest PUBLISHED for a code, resolving tenant-override -> global (-1).
     *
     * <p>Ordered tenant-first (real ids are > -1), then newest version. So a
     * tenant that has forked a flow gets their own; everyone else inherits the
     * product flow. Without the -1 arm, moving the catalogue global would make
     * startInstance fail for every workflow.
     */
    @Query("SELECT w FROM WorkflowDefinition w " +
            "WHERE w.tenantId IN (:tenantId, -1) AND w.code = :code AND w.status = 'PUBLISHED' " +
            "ORDER BY w.tenantId DESC, w.version DESC")
    java.util.List<WorkflowDefinition> findLatestPublished(
            @Param("tenantId") Long tenantId, @Param("code") String code);

    default Optional<WorkflowDefinition> findLatestPublishedSingle(Long tenantId, String code) {
        return findLatestPublished(tenantId, code).stream().findFirst();
    }

    /**
     * Every PUBLISHED definition visible to a tenant (its own forks + product
     * globals at -1). Used to resolve which states a given page acts on, for
     * the page-scoped work queue.
     */
    @Query("SELECT w FROM WorkflowDefinition w " +
            "WHERE w.tenantId IN (:tenantId, -1) AND w.status = 'PUBLISHED'")
    java.util.List<WorkflowDefinition> findAllPublished(@Param("tenantId") Long tenantId);

    /**
     * Exact definition by (owner, code, version).
     *
     * <p>The owner is the tenant that OWNS the definition (-1 for a product flow,
     * N for a tenant's fork) — not the tenant running the instance. An instance
     * records its owner at start so it keeps resolving the same body even after
     * the tenant later forks the flow and starts its own version numbering.
     */
    Optional<WorkflowDefinition> findByTenantIdAndCodeAndVersion(Long tenantId, String code, Integer version);
}
