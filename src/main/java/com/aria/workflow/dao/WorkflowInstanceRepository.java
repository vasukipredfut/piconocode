package com.aria.workflow.dao;

import com.aria.workflow.model.WorkflowInstance;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkflowInstanceRepository extends JpaRepository<WorkflowInstance, Long> {

    Optional<WorkflowInstance> findByIdAndTenantId(Long id, Long tenantId);

    /**
     * SELECT … FOR UPDATE to serialise concurrent page submissions against
     * the same instance. The transaction in {@code WorkflowEngineService}
     * holds the row lock for the duration of the transition.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM WorkflowInstance w WHERE w.id = :id AND w.tenantId = :tenantId")
    Optional<WorkflowInstance> findByIdForUpdate(@Param("id") Long id, @Param("tenantId") Long tenantId);

    @Query("SELECT w FROM WorkflowInstance w " +
            "WHERE w.tenantId = :tenantId AND w.assignedRole = :persona AND w.status = 'OPEN' " +
            "ORDER BY w.updatedAt DESC")
    Page<WorkflowInstance> queueForPersona(
            @Param("tenantId") Long tenantId,
            @Param("persona") String persona,
            Pageable pageable);
}
