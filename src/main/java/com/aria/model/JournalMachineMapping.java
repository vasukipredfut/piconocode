package com.aria.model;

import com.aria.dto.AuxiliaryMachineMappingDto;
import com.aria.dto.JournalMachineMappingDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "journal_machine_mapping")
public class JournalMachineMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "branch_id")
    private Long branchId;

    @Column(name = "dept_id")
    private Long deptId;

    @Column(name = "journal_id")
    private Long journalId;

    @Column
    private Long primary_machine_id;

    @Column
    private String primary_machine_name;

    @Column(name = "machine_id")
    private Long machineId;

    @Column(name = "machine_name")
    private String machineName;


    @Column(name = "primary_work_center")
    private String primaryWorkCenter;

    @Column(name = "display_name")
    private String displayName;

    @Column
    private String connect_table;

    @Column
    private Boolean status;


    public JournalMachineMapping(JournalMachineMappingDto journalMachineMapping) {

        this.id = journalMachineMapping.getId();
        this.journalId = journalMachineMapping.getJournalId();
        this.machineId = journalMachineMapping.getMachineId();
        this.machineName = journalMachineMapping.getMachineName();
        this.primaryWorkCenter = journalMachineMapping.getPrimaryWorkCenter();
        this.primary_machine_id = journalMachineMapping.getMachineId();
        this.primary_machine_name = journalMachineMapping.getMachineName();
        this.displayName = journalMachineMapping.getDisplayName();
        this.connect_table = journalMachineMapping.getConnectTable();
    }


    public JournalMachineMapping(AuxiliaryMachineMappingDto journalMachineMapping) {

        this.id = journalMachineMapping.getId();
        this.journalId = journalMachineMapping.getJournalId();
        this.machineId = journalMachineMapping.getMachineId();
        this.machineName = journalMachineMapping.getMachineName();
        this.primaryWorkCenter = journalMachineMapping.getPrimaryWorkCenter();
        this.primary_machine_id = journalMachineMapping.getPrimaryMachineId();
        this.primary_machine_name = journalMachineMapping.getPrimaryMachineName();
        this.displayName = journalMachineMapping.getDisplayName();
        this.connect_table = journalMachineMapping.getConnectTable();
    }
}
