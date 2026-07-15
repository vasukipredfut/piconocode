package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FlowDto {



    private Long workFlowId;

    private String flowName;

    private String flowParentName;


}
