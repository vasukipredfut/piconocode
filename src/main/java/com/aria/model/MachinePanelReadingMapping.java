package com.aria.model;

import com.aria.dto.MachinePanelReadingMappingDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "machine_workflow_panel_reading")
public class MachinePanelReadingMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "work_flow_id")
    private Long workFlowId;

    @Column(name = "machine_id")
    private Long machineId;

    @Column(name = "panel_id")
    private Long panelId;

    @Column(name = "panel_name")
    private String panelName;

    @Column(name = "meter_open_reading")
    private String meterOpenReading;

    @Column(name = "meter_closing_reading")
    private String meterClosingReading;

    public MachinePanelReadingMapping(MachinePanelReadingMappingDto machinePanelReadingMapping) {
        this.id = machinePanelReadingMapping.getId();
        this.workFlowId = machinePanelReadingMapping.getWorkFlowId();
        this.machineId = machinePanelReadingMapping.getMachineId();
        this.panelId = machinePanelReadingMapping.getPanelId();
        this.panelName = machinePanelReadingMapping.getPanelName();
        this.meterOpenReading = machinePanelReadingMapping.getMeterOpenReading();
        this.meterClosingReading = machinePanelReadingMapping.getMeterClosingReading();
    }

    public MachinePanelReadingMapping(MachinePanelReadingMapping readingMapping, MachinePanelReadingMappingDto machinePanelReadingMapping) {
        this.id = readingMapping.getId();
        this.workFlowId = readingMapping.getWorkFlowId();
        this.machineId = readingMapping.getMachineId();
        this.panelId = readingMapping.getPanelId();
        this.panelName = machinePanelReadingMapping.getPanelName();
        this.meterOpenReading = machinePanelReadingMapping.getMeterOpenReading();
        this.meterClosingReading = machinePanelReadingMapping.getMeterClosingReading();
    }
}
