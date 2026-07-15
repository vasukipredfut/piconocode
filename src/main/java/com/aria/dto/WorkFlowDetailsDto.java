package com.aria.dto;


import com.aria.model.WorkFlowDetails;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class WorkFlowDetailsDto {

    private Long id;

    private Long workFlowId;

    private String workFlowName;

    private String type;

    private Long typeId;

    private Long createdBy;

    private Long updatedBy;

    private LocalDateTime created_date;

    private LocalDateTime updated_date;

    private List<WorkFlowStepsDto> workFlowSteps;


    public WorkFlowDetailsDto(WorkFlowDetails workFlowDetails) {
        this.id = workFlowDetails.getId();
        if (workFlowDetails.getWorkFlow() != null) {
            this.workFlowId = workFlowDetails.getWorkFlow().getId();
            this.workFlowName = workFlowDetails.getWorkFlow().getWorkFlowName();
        }
        this.createdBy = workFlowDetails.getCreatedBy();
        this.updatedBy = workFlowDetails.getUpdatedBy();
        this.created_date = workFlowDetails.getCreated_date();
        this.updated_date = workFlowDetails.getUpdated_date();
    }
}
