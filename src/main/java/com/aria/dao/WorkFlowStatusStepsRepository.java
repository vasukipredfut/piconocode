package com.aria.dao;

import com.aria.model.WorkFlowStatusSteps;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkFlowStatusStepsRepository extends JpaRepository<WorkFlowStatusSteps, Long> {

}
