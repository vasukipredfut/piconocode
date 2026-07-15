package com.aria.dto;


import com.aria.model.WorkFlow;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
public class WorkFlowDto {

    private Long id;

    private String workFlowName;

    private Long createdBy;

    private Long updatedBy;

    private LocalDateTime created_date;

    private LocalDateTime updated_date;


    public WorkFlowDto(WorkFlow workFlow) {

        this.id = workFlow.getId();

        this.workFlowName = workFlow.getWorkFlowName();

        this.createdBy = workFlow.getCreatedBy();

        this.updatedBy = workFlow.getUpdatedBy();

        this.created_date = workFlow.getCreated_date();

        this.updated_date = workFlow.getUpdated_date();

    }
}
