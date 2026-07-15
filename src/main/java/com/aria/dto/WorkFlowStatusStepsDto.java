package com.aria.dto;

import com.aria.model.WorkFlowStatusSteps;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WorkFlowStatusStepsDto {


    private Long id;

    private Long flowId;

    private int stepNumber;

    private String stepName;

    private String status;


    public WorkFlowStatusStepsDto(WorkFlowStatusSteps flowStatusSteps) {
        this.id = flowStatusSteps.getId();
        this.flowId = flowStatusSteps.getFlowId();
        this.stepNumber = flowStatusSteps.getStepNumber();
        this.stepName = flowStatusSteps.getStepName();
        this.status = flowStatusSteps.getStatus();
    }
}
