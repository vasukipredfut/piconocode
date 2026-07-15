package com.aria.dto;

import com.aria.model.MachinePanelReadingMapping;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MachinePanelReadingMappingDto {

    private Long id;
    private Long workFlowId;
    private Long machineId;
    private Long panelId;
    private String panelName;
    private String meterOpenReading;
    private String meterClosingReading;

    public MachinePanelReadingMappingDto(MachinePanelReadingMapping machinePanelReadingMapping) {
        this.id = machinePanelReadingMapping.getId();
        this.workFlowId = machinePanelReadingMapping.getWorkFlowId();
        this.machineId = machinePanelReadingMapping.getMachineId();
        this.panelId = machinePanelReadingMapping.getPanelId();
        this.panelName = machinePanelReadingMapping.getPanelName();
        this.meterOpenReading = machinePanelReadingMapping.getMeterOpenReading();
        this.meterClosingReading = machinePanelReadingMapping.getMeterClosingReading();
    }
}
