package com.aria.dao;


import com.aria.model.WorkFlowSteps;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkFlowStepsRepository extends JpaRepository<WorkFlowSteps, Long> {


    @Query(value = "SELECT *  FROM work_flow_steps w WHERE w.workflow_id =:workflowId", nativeQuery = true)
    List<WorkFlowSteps> findAllByFlowId(@Param("workflowId") Long workflowId);
}
