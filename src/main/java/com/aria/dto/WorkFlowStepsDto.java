package com.aria.dto;

import com.aria.model.WorkFlowSteps;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class WorkFlowStepsDto {

    private Long id;


    private Long workflowId;


    private int stepNumber;


    private String stepName;

    public WorkFlowStepsDto(WorkFlowSteps workFlowSteps) {
        this.id = workFlowSteps.getId();
        this.workflowId = workFlowSteps.getWorkflowId();
        this.stepNumber = workFlowSteps.getStepNumber();
        this.stepName = workFlowSteps.getStepName();
    }
}
