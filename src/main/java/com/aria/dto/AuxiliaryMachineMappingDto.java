package com.aria.dto;

import com.aria.model.JournalMachineMapping;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class AuxiliaryMachineMappingDto {

    private Long id;

    private Long journalId;

    private Long primaryMachineId;

    private Long machineId;

    private String machineName;

    private String primaryMachineName;

    private String primaryWorkCenter;

    private String displayName;

    private String connectTable;

    private Long branchId;

    private Long deptId;

    private List<JournalAttributesMappingDto> dataSetList;

    public AuxiliaryMachineMappingDto(JournalMachineMapping journalMachineMapping) {
        this.id = journalMachineMapping.getId();
        this.journalId = journalMachineMapping.getJournalId();
        this.branchId = journalMachineMapping.getBranchId();
        this.deptId = journalMachineMapping.getDeptId();
        this.machineName = journalMachineMapping.getMachineName();
        this.machineId = journalMachineMapping.getMachineId();
        this.primaryWorkCenter = journalMachineMapping.getPrimaryWorkCenter();
        this.primaryMachineId = journalMachineMapping.getPrimary_machine_id();
        this.primaryMachineName = journalMachineMapping.getPrimary_machine_name();
        this.connectTable = journalMachineMapping.getConnect_table();
        this.displayName = journalMachineMapping.getDisplayName();
    }
}
