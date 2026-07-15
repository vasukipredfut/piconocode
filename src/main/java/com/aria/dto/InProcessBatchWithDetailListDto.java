package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class InProcessBatchWithDetailListDto {

    private Long machineId;
    private String machineName;
    private String machineType;
    private String binId;
    private Long binName;


    public InProcessBatchWithDetailListDto(InProcessBatchWithDetailList inProcessBatchList) {
        this.machineId = inProcessBatchList.getMachineId();
        this.machineName = inProcessBatchList.getMachineName();
        this.binId = inProcessBatchList.getBinId();
        this.binName = inProcessBatchList.getBinName();
    }
}
