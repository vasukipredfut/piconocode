package com.aria.m0.workflow.dao;

import com.aria.m0.workflow.model.WorkflowEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowEventRepository extends JpaRepository<WorkflowEvent, Long> {

    List<WorkflowEvent> findByInstanceIdOrderByTsDesc(Long instanceId, Pageable pageable);

    List<WorkflowEvent> findByInstanceIdOrderByTsAsc(Long instanceId);
}
