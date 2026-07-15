package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ProductionEntryGroupingDto {

    private String journalName;
    private Long journalId;
    private String tsName;
    private Long tsId;
    private Long batchId;
    private String batchNo;
    private String trnNo;
    private Long machineId;
    private Long auxMachineId;
    private Long shiftId;
    private Date shiftDate;
    List<ProductionEntryGroupingListDto> slabDetails;

    public ProductionEntryGroupingDto(ProductionEntryGrouping productionEntryGrouping) {
        this.journalName = productionEntryGrouping.getJournalName();
        this.journalId = productionEntryGrouping.getJournalId();
        this.tsName = productionEntryGrouping.getTsName();
        this.tsId = productionEntryGrouping.getTsId();
        this.batchId = productionEntryGrouping.getBatchId();
        this.batchNo = productionEntryGrouping.getBatchNo();
        this.trnNo = productionEntryGrouping.getTrnNo();
        this.machineId = productionEntryGrouping.getMachineId();
        this.auxMachineId = productionEntryGrouping.getAuxMachineId();
        this.shiftId = productionEntryGrouping.getShiftId();
        this.shiftDate = productionEntryGrouping.getShiftDate();
    }
}
