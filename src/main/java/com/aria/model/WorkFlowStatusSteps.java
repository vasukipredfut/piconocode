package com.aria.model;

import com.aria.dto.WorkFlowStatusStepsDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "work_flow_status")
public class WorkFlowStatusSteps {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "flow_Id")
    private Long flowId;

    @Column(name = "step_Number")
    private int stepNumber;

    @Column(name = "step_Name")
    private String stepName;

    @Column(name = "status")
    private String status;

    public WorkFlowStatusSteps(WorkFlowStatusStepsDto flowStatusSteps) {
        this.id = flowStatusSteps.getId();
        this.flowId = flowStatusSteps.getFlowId();
        this.stepNumber = flowStatusSteps.getStepNumber();
        this.stepName = flowStatusSteps.getStepName();
        this.status = flowStatusSteps.getStatus();
    }
}
