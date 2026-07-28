package com.aria.workflow.dao;

import com.aria.workflow.model.WorkflowEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowEventRepository extends JpaRepository<WorkflowEvent, Long> {

    List<WorkflowEvent> findByInstanceIdOrderByTsDesc(Long instanceId, Pageable pageable);

    List<WorkflowEvent> findByInstanceIdOrderByTsAsc(Long instanceId);

    /** The transition that landed the instance on its current state — its
     *  fromState is "the state that was just completed". */
    java.util.Optional<WorkflowEvent> findFirstByInstanceIdAndKindOrderByTsDesc(Long instanceId, String kind);
}
