package com.aria.model;

import com.aria.dto.WorkFlowStepsDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "work_flow_steps")
public class WorkFlowSteps {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "workflow_Id")
    private Long workflowId;

    @Column(name = "step_Number")
    private int stepNumber;

    @Column(name = "step_Name")
    private String stepName;


    public WorkFlowSteps(WorkFlowStepsDto workFlowSteps) {
        this.id = workFlowSteps.getId();
        this.workflowId = workFlowSteps.getWorkflowId();
        this.stepNumber = workFlowSteps.getStepNumber();
        this.stepName = workFlowSteps.getStepName();
    }
}
