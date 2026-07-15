package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductionEntryMachineGroupingDto {

    private Long journalId;

    private String journalName;
    private Long machineId;
    private String machineName;
    private Long auxMachineId;
    private String auxMachineName;


    public ProductionEntryMachineGroupingDto(ProductionEntryMachineGrouping machineGrouping) {
        this.journalId = machineGrouping.getJournalId();
        this.journalName = machineGrouping.getJournalName();
        this.machineId = machineGrouping.getMachineId();
        this.machineName = machineGrouping.getMachineName();
        this.auxMachineId = machineGrouping.getAuxMachineId();
        this.auxMachineName = machineGrouping.getAuxMachineName();
    }
}
