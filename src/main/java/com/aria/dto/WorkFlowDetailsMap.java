package com.aria.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.aria.model.WorkFlow;
import com.aria.model.WorkFlowDetails;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
public class WorkFlowDetailsMap {

    private String taskName;
    private String successorsteponFailure;
    private String successorsteponSuccess;
    private String precedessorstep;

    List<WorkFlowDetailsMap> workFlowDetails = new ArrayList<>();

    public WorkFlowDetailsMap(WorkFlowDetails workFlow) {
        this.taskName = workFlow.getFlowName();
    }
}
